package es.upm.fi.blockchain.upmpoly;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public final class Player {


    @Property()
    private final String playerID;

    @Property()
    private final String name;

    @Property()
    private final int credit;

    @Property()
    private final boolean isEliminated;

    public String getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }

    public int getCredit() {
        return credit;
    }

    public boolean getIsEliminated() {
        return isEliminated;
    }

    public Player(@JsonProperty("playerID") final String playerID, @JsonProperty("name") final String name,
                 @JsonProperty("credit") final int credit) {
        this.playerID = playerID;
        this.name = name;
        this.credit = credit;
        this.isEliminated = false;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Player other = (Player) obj;

        return Objects.deepEquals(
                new String[] {getPlayerID(), getName()},
                new String[] {other.getPlayerID(), other.getName()})
                &&
                Objects.deepEquals(
                        new int[] {getCredit()},
                        new int[] {other.getCredit()})
                &&
                Objects.deepEquals(
                        new boolean[] {getIsEliminated()},
                        new boolean[] {other.getIsEliminated()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerID(), getName(), getCredit(), getIsEliminated());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [playerID=" + playerID + ", name="
                + name + ", credit=" + credit + ", isEliminated=" + isEliminated + "]";
    }
}
