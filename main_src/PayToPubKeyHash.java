package edu.nyu.crypto.csci3033.transactions;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.spongycastle.asn1.util.Dump;

import java.io.File;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_VERIFY;

/**
 * Created by bbuenz on 24.09.15.
 */
public class PayToPubKeyHash extends ScriptTransaction {
    private ECKey vanity;

    // Problem 1: First attempt on testnet, then on main net with real money
    public PayToPubKeyHash(NetworkParameters parameters, File file, String password) {
        super(parameters, file, password);
        String privateString = "my_private_secret_key ;)";
        // Need to catch/throw exception (to avoid unreported exception)
        try {
            DumpedPrivateKey dumped_key = new DumpedPrivateKey(parameters, privateString);
            System.out.print("Imported well");
            vanity = dumped_key.getKey();
        } catch (AddressFormatException e){};



    }

    @Override
    public Script createInputScript() {
        ScriptBuilder builder = new ScriptBuilder();
        builder.op(OP_DUP);
        builder.op(OP_HASH160);
        builder.data(vanity.getPubKeyHash());
        builder.op(OP_EQUALVERIFY);
        builder.op(OP_CHECKSIG);
        return builder.build();
        // TODO: Create a P2PKH script
        // TODO: be sure to test this script on the mainnet using a vanity address
    }

    @Override
    public Script createRedemptionScript(Transaction unsignedTransaction) {
        TransactionSignature txSig = sign(unsignedTransaction, vanity);

        ScriptBuilder builder = new ScriptBuilder();
        builder.data(txSig.encodeToBitcoin());
        builder.data(vanity.getPubKey());
        return builder.build();
    }
}
