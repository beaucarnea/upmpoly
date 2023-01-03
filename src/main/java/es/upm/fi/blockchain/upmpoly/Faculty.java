package es.upm.fi.blockchain.upmpoly;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public final class Faculty {

    @Property()
    private final String facultyID;

    @Property()
    private final String name;

    @Property()
    private final int salePrice;

    @Property()
    private final int rentalFee;

    @Property()
    private final String owner;

    public String getFacultyID() {
        return facultyID;
    }

    public String getName() {
        return name;
    }

    public int getSalePrice() {
        return salePrice;
    }

    public int getRentalFee() {
        return rentalFee;
    }

    public String getOwner() {
        return owner;
    }

    public Faculty(@JsonProperty("facultyID") final String facultyID, @JsonProperty("name") final String name,
                   @JsonProperty("salePrice") final int salePrice, @JsonProperty("rentalFee") final int rentalFee,
                   @JsonProperty("owner") final String owner) {
        this.facultyID = facultyID;
        this.name = name;
        this.salePrice = salePrice;
        this.rentalFee = rentalFee;
        this.owner = owner;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Faculty other = (Faculty) obj;

        return Objects.deepEquals(
                new String[] {getFacultyID(), getName(), getOwner()},
                new String[] {other.getFacultyID(), other.getName(), other.getOwner()})
                &&
                Objects.deepEquals(
                        new int[] {getSalePrice(), getRentalFee()},
                        new int[] {other.getSalePrice(), other.getRentalFee()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFacultyID(), getName(), getSalePrice(), getRentalFee(), getOwner());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [facultyID=" + facultyID + ", name="
                + name + ", salePrice=" + salePrice + ", owner=" + owner + ", rentalFee=" + rentalFee + "]";
    }
}
