package es.upm.fi.blockchain.upmpoly;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "upmpoly",
        info = @Info(
                title = "UPM poly",
                description = "The hyperlegendary upm poly",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "upm.poly@upm.es",
                        name = "Nicco Marius Joao",
                        url = "https://www.fi.upm.es/")))
@Default
public final class UpmPoly implements ContractInterface {

    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS,
        FACULTY_AREADY_OWNED,
        PLAYER_BROKE
    }

    /**
     * Creates some initial assets on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        Player(ctx, "player1", "Joao", 5);
        Player(ctx, "player2", "Nicco", 10);
        Player(ctx, "player3", "Marius", 10000000);

        Faculty(ctx, "faculty1", "ComputerScience", 2000000, 5000);
        Faculty(ctx, "faculty2", "DataScience", 1500000, 4000);
        Faculty(ctx, "faculty3", "Business", 1000000, 3000);
    }

    /**
     *
     * creates a new player on the ledger
     *
     * @param ctx the transaction context
     * @param playerId the Id of the player
     * @param name the name of the player
     * @param money the amount of money the plyer has in his account
     * @return the created player
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Player Player(final Context ctx, final String playerId, final String name, final int money) {

        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, playerId)) {
            String errorMessage = String.format("Player %s already exists", playerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UpmPoly.AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Player player = new Player(playerId, name, money);
        String playerJSON = genson.serialize(player);
        stub.putStringState(playerId, playerJSON);

        return player;
    }

    /**
     *
     * creates a new faculty on the ledger
     *
     * @param ctx the transaction context
     * @param facultyId the id of the faculty
     * @param name the name of the faculty
     * @param salePrice the sale price of the faculty
     * @param rentalFee the rantal fee of the faculty
     * @return the created faculty
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Faculty Faculty(final Context ctx, final String facultyId, final String name, final int salePrice, final int rentalFee) {

        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, facultyId)) {
            String errorMessage = String.format("Faculty %s already exists", facultyId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UpmPoly.AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Faculty faculty = new Faculty(facultyId, name, salePrice, rentalFee, null);
        String playerJSON = genson.serialize(faculty);
        stub.putStringState(facultyId, playerJSON);

        return faculty;
    }

    /**
     * Retrieves a player with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param playerId the ID of the asset
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Player ReadPlayer(final Context ctx, final String playerId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(playerId);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Player %s does not exist", playerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Player player = genson.deserialize(assetJSON, Player.class);
        return player;
    }

    /**
     * Retrieves a faculty with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param facultyId the ID of the asset
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Faculty ReadFaculty(final Context ctx, final String facultyId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(facultyId);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Player %s does not exist", facultyId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Faculty faculty = genson.deserialize(assetJSON, Faculty.class);
        return faculty;
    }

    /**
     *
     * updates the ownership of an faculty and adjust the account balance of the buyer
     *
     * @param ctx the transaction context
     * @param playerId the id of the buying player
     * @param facultyId the id of the target faculty
     * @return the new created faculty asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Faculty buyFaculty(final Context ctx, final String playerId, final String facultyId) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, playerId)) {
            String errorMessage = String.format("Asset %s does not exist", playerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        if (!AssetExists(ctx, facultyId)) {
            String errorMessage = String.format("Asset %s does not exist", facultyId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Player oldPlayer = this.ReadPlayer(ctx, playerId);
        Faculty oldFaculty = this.ReadFaculty(ctx, facultyId);

        if (oldFaculty.getOwner() != null) {
            String errorMessage = String.format("Faculty %1$s atready owned by player %2$s", oldFaculty.getFacultyID(), oldPlayer.getPlayerID());
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.FACULTY_AREADY_OWNED.toString());
        }

        int accountBalance = oldPlayer.getCredit() - oldFaculty.getSalePrice();
        if (accountBalance < 0) {
            String errorMessage = String.format("Player %1$s dont have enough money to buy %2$s", oldPlayer.getPlayerID(), oldFaculty.getFacultyID());
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.PLAYER_BROKE.toString());
        }

        Player newPlayer = new Player(playerId, oldPlayer.getName(), accountBalance);
        Faculty newFaculty = new Faculty(facultyId, oldFaculty.getName(), oldFaculty.getSalePrice(), oldFaculty.getRentalFee(), playerId);

        String newPlayerJSON = genson.serialize(newPlayer);
        stub.putStringState(playerId, newPlayerJSON);

        String newFacultyJSON = genson.serialize(newFaculty);
        stub.putStringState(playerId, newFacultyJSON);

        return newFaculty;
    }

    /**
     * Checks the existence of the asset on the ledger
     *
     * @param ctx the transaction context
     * @param number the ID of the asset
     * @return boolean indicating the existence of the asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String number) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(number);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Retrieves all players from the ledger.
     *
     * @param ctx the transaction context
     * @return array of players found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllPlayers(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Player> queryResults = new ArrayList<Player>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Player player = genson.deserialize(result.getStringValue(), Player.class);
            queryResults.add(player);
            System.out.println(player.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    /**
     * Retrieves all faculties from the ledger.
     *
     * @param ctx the transaction context
     * @return array of faculties found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllFaculties(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Faculty> queryResults = new ArrayList<Faculty>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Faculty faculty = genson.deserialize(result.getStringValue(), Faculty.class);
            queryResults.add(faculty);
            System.out.println(faculty.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
