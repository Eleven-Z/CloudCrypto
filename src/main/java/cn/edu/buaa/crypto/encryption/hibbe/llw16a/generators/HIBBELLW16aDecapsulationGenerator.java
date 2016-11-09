package cn.edu.buaa.crypto.encryption.hibbe.llw16a.generators;

import cn.edu.buaa.crypto.utils.PairingUtils;
import cn.edu.buaa.crypto.encryption.hibbe.llw16a.serparams.HIBBELLW16ACipherSerParameter;
import cn.edu.buaa.crypto.encryption.hibbe.llw16a.genparams.HIBBELLW16aDecapsulationParameter;
import cn.edu.buaa.crypto.encryption.hibbe.llw16a.serparams.HIBBELLW16aPublicKeySerParameter;
import cn.edu.buaa.crypto.encryption.hibbe.llw16a.serparams.HIBBELLW16aSecretKeySerParameter;
import cn.edu.buaa.crypto.algebra.generators.PairingDecapsulationGenerator;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

/**
 * Created by Weiran Liu on 2016/5/17.
 *
 * Liu-Liu-Wu prime-order HIBBE session key decapsulation generator.
 */
public class HIBBELLW16aDecapsulationGenerator implements PairingDecapsulationGenerator {
    private HIBBELLW16aDecapsulationParameter params;

    public void init(CipherParameters params) {
        this.params = (HIBBELLW16aDecapsulationParameter)params;
    }

    public byte[] recoverKey() throws InvalidCipherTextException {
        HIBBELLW16aPublicKeySerParameter publicKeyParameters = this.params.getPublicKeyParameters();
        HIBBELLW16aSecretKeySerParameter secretKeyParameters = this.params.getSecretKeyParameters();
        HIBBELLW16ACipherSerParameter ciphertextParameters = this.params.getCiphertextParameters();

        Pairing pairing = PairingFactory.getPairing(publicKeyParameters.getParameters());
        Element[] elementIdsCT = PairingUtils.MapToZr(pairing, this.params.getIds());

        for (int i=0; i<publicKeyParameters.getMaxUser(); i++){
            if (secretKeyParameters.getIdAt(i) != null &&
                    !secretKeyParameters.getElementIdAt(i).equals(elementIdsCT[i])){
                throw new InvalidCipherTextException("Secret Key identity vector does not match Ciphertext identity vector set");
            }
        }

        Element a0 = secretKeyParameters.getA0().getImmutable();
        Element C0 = ciphertextParameters.getC0().getImmutable();
        Element C1 = ciphertextParameters.getC1().getImmutable();
        Element a1 = secretKeyParameters.getA1().getImmutable();

        for (int i=0; i<publicKeyParameters.getMaxUser(); i++){
            if (secretKeyParameters.getIdAt(i) == null && params.getIdsAt(i) != null) {
                a0 = a0.mul(secretKeyParameters.getBsAt(i).powZn(elementIdsCT[i])).getImmutable();
            }
        }
        Element temp0 = pairing.pairing(C0, a0).getImmutable();
        Element temp1 = pairing.pairing(a1, C1).getImmutable();
        Element sessionKey = temp0.div(temp1).getImmutable();
        return sessionKey.toBytes();
    }
}