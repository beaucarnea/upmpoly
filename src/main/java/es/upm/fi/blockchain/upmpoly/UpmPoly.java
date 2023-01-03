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
        ASSET_ALREADY_EXISTS
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

    }

    /**
     *
     * @param ctx
     * @param number
     * @param name
     * @param money
     * @return
     */
    public Player Player(final Context ctx, final String number, final String name, final int money) {

        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, number)) {
            String errorMessage = String.format("Asset %s already exists", number);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, UpmPoly.AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Player player = new Player(number, name, money);
        String playerJSON = genson.serialize(player);
        stub.putStringState(number, playerJSON);

        return player;
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

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Player player = genson.deserialize(result.getStringValue(), Player.class);
            queryResults.add(player);
            System.out.println(player.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
