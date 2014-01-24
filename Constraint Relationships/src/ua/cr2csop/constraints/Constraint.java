package ua.cr2csop.constraints;

/**
 * Simple class to represent a constraint in a DAG of constraints connected by
 * the precedes-relation Weights are to be calculated in breadth-first search
 * 
 */
public class Constraint {
    private final String name;
    private int weight;

    public Constraint(String name) {
        this(name, 1);
    }

    public Constraint(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public Constraint(String name, String i) {
        this.name = name;
        this.weight = Integer.parseInt(i);
    }

    public String getName() {
        return this.name;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int i) {
        this.weight = i;
    }

    @Override
    public String toString() {
        return String.format("Constraint : %s; Weight: %s", name, weight);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof Constraint))
            return false;

        Constraint other = (Constraint) obj;
        if (name == null && other.name != null) {
            return false;
        }

        return name.equals(other.name);
    }

}
