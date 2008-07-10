package bingo.game;

import java.security.*;
import bingo.shared.*;

class NotaryPublic {

    private PrivateKey priv = null;
    private PublicKey pub = null;

    NotaryPublic() {
        KeyPair pair = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            keyGen.initialize(1024, new SecureRandom());
            pair = keyGen.generateKeyPair();
        } catch (Exception e) {
	    ErrorMessages.error("Cannot sign cards. Continuing anyway.");
        }
	priv = pair.getPrivate();
	pub = pair.getPublic();
    }

    void signTheCard(Card c, int gameNumber) throws NoSuchAlgorithmException,
					            InvalidKeyException,
					            SignatureException {
        Signature dsa = Signature.getInstance("SHA/DSA");
	byte[] values = new byte[Card.SIZE*Card.SIZE+1];

        dsa.initSign(priv);

        for (int i = 0; i < Card.SIZE; i++)
            for (int j = 0; j < Card.SIZE; j ++)
		values[Card.SIZE*i + j] = (byte)c.boardValues[i][j].number;
	values[values.length-1] = (byte)gameNumber;

	dsa.update(values);
	c.setSignature(dsa.sign());
    }

    boolean verifyTheSignature(Card c, int gameNumber) {
        try {
            Signature dsa = Signature.getInstance("SHA/DSA");
	    byte[] values = new byte[Card.SIZE*Card.SIZE+1];

            dsa.initVerify(pub);

            for (int i = 0; i < Card.SIZE; i ++)
                for (int j = 0; j < Card.SIZE; j ++)
                    values[Card.SIZE*i + j] = (byte)c.boardValues[i][j].number;
	    values[values.length-1] = (byte)gameNumber;

	    dsa.update(values);
            return dsa.verify(c.getSignature());
        } catch (Exception e) {
            return false;
        }
    }
}
