package org.hyperledger.fabric.sdk.security;

import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;

import org.bouncycastle.crypto.params.ECPrivateKeyParameters;

public class SM2PrivateKey implements ECPrivateKey,java.io.Serializable {
    private String          algorithm = "EC";

	private transient BigInteger              d;

	public SM2PrivateKey(String algorithm,ECPrivateKeyParameters params){
		this.algorithm = algorithm;
		this.d= params.getD();
	}
	
	@Override
	public String getAlgorithm() {
		
		return algorithm;
	}

	@Override
	public String getFormat() {
        return "PKCS#8";
	}

	@Override
	public byte[] getEncoded() {
		return d.toByteArray();
	}

	@Override
	public ECParameterSpec getParams() {
		return null;
	}

	@Override
	public BigInteger getS() {
		return d;
	}
	
	public BigInteger getD() {
		return d;
	}

}
