import java.util.*;
import java.io.*;

public class PR198
{
    public interface Filter
    {
	public boolean filter(Object rs, StringBuffer sb);
    }

    private static class SubspecFilter implements Filter
    {
	public boolean
	filter(Object rs, StringBuffer sb)
	{
	    return true;
	}
    }

    static class Compiler {}

    static class Match {
	// Whats odd is that the core dump changes location
	// if you coment out the following method!
	void regtry(int off) {}
    }
}
