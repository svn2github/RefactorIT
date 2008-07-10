class Employee1 {
    static final int ENGINEER=0;
    static final int SALESMAN=1;
    static final int MANAGER=2;
    private EmployeeType _type;
    int _monthlySalary;

    private int _bonus;
    private int _commission;

    /**
     * @param _type
     */
    public Employee1(int _type) {
        this.setType(_type);
    }

    int payAmount()
    {
        switch(getType())
        {
            case ENGINEER:
                return _monthlySalary;
            case SALESMAN:
                return _monthlySalary+_commission;
            case MANAGER:
                return _monthlySalary+_bonus;
            default:
                throw new RuntimeException("Invalid employee");
        }
    }

    public int getType() {
        return this._type.getTypeCode();
    }

    public void setType(final int _type) {
        switch( _type )
        {
            case ENGINEER:
                this._type =new Engineer1();
                break;
            case MANAGER :
                this._type=new Manager(5);
                break;
            case SALESMAN:
                this._type=new Salesman();
        }
    }
}

abstract class EmployeeType {
    abstract int getTypeCode();

}

class Engineer1 extends EmployeeType {
    int getTypeCode() {
        return Employee1.ENGINEER;
    }
}
class Manager extends EmployeeType {
    Manager(int code)
    {
    }
    int getTypeCode() {
        return Employee1.MANAGER;
    }

}

class Salesman extends EmployeeType
{

    int getTypeCode() {
        return Employee1.SALESMAN;
    }

}
