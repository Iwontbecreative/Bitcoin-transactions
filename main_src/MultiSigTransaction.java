package edu.nyu.crypto.csci3033.transactions;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.File;
import java.math.BigInteger;

import static org.bitcoinj.script.ScriptOpCodes.*;

/**
 * Created by bbuenz on 24.09.15.
 */
public class MultiSigTransaction extends ScriptTransaction {
    private DeterministicKey bank;
    // TODO: Look up arrays in java
    private DeterministicKey client1;
    private DeterministicKey client2;
    private DeterministicKey client3;

    public MultiSigTransaction(NetworkParameters parameters, File file, String password) {
        super(parameters, file, password);
        bank = getWallet().freshReceiveKey();
        client1 = getWallet().freshReceiveKey();
        client2 = getWallet().freshReceiveKey();
        client3 = getWallet().freshReceiveKey();
    }

    // Steal bignum implementation from bitcoinj.


    @Override
    public Script createInputScript() {
        // TODO: Create a script that can be spend using signatures from the bank and one of the customers
        ScriptBuilder builder = new ScriptBuilder();

        // Start by checking for the bank, to simplify we'll assume it is the first key
        builder.data(bank.getPubKey());
        builder.op(OP_CHECKSIGVERIFY);
        // Now at least 1-of-3 clients has to sign
        builder.smallNum(1);
        builder.data(client1.getPubKey());
        builder.data(client2.getPubKey());
        builder.data(client3.getPubKey());
        builder.smallNum(3);
        builder.op(OP_CHECKMULTISIG);
        return builder.build();
    }

    @Override
    public Script createRedemptionScript(Transaction unsignedTransaction) {
        // Please be aware of the CHECK_MULTISIG bug
        TransactionSignature bankSig = sign(unsignedTransaction, bank);
        TransactionSignature clientSig = sign(unsignedTransaction, client1);
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(1); // Work around the check multisig bug.
        // 0 does not work,, see https://github.com/bitcoinj/bitcoinj/issues/1086
        // This leads to warnings however: non-mandatory-script-verify-flag (Dummy CHECKMULTISIG argument must be zero)
        builder.data(clientSig.encodeToBitcoin());
        builder.data(bankSig.encodeToBitcoin());
        return builder.build();
    }
}
