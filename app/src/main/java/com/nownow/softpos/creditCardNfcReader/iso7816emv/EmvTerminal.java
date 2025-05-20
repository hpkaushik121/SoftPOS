package com.aicortex.softpos.creditCardNfcReader.iso7816emv;

//import static id.paypro.nfc.creditCardNfcReader.parser.EmvParser.padleft;

import static com.aicortex.softpos.creditCardNfcReader.parser.EmvParser.padleft;

import com.aicortex.softpos.creditCardNfcReader.model.enums.CountryCodeEnum;
import com.aicortex.softpos.creditCardNfcReader.model.enums.CurrencyEnum;
import com.aicortex.softpos.creditCardNfcReader.model.enums.TransactionTypeEnum;
import com.aicortex.softpos.creditCardNfcReader.parser.EmvParser;
import com.aicortex.softpos.creditCardNfcReader.terminalData.TerminalVerificationResults;
import com.aicortex.softpos.creditCardNfcReader.utils.BytesUtils;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;

/*import id.paypro.nfc.creditCardNfcReader.model.enums.CountryCodeEnum;
import id.paypro.nfc.creditCardNfcReader.model.enums.CurrencyEnum;
import id.paypro.nfc.creditCardNfcReader.model.enums.TransactionTypeEnum;
import id.paypro.nfc.creditCardNfcReader.parser.EmvParser;
import id.paypro.nfc.creditCardNfcReader.terminalData.TerminalVerificationResults;
import id.paypro.nfc.creditCardNfcReader.utils.BytesUtils;*/

/**
 * Factory to create Tag value
 */
public final class EmvTerminal {

	/**
	 * Random
	 */
	private static final SecureRandom random = new SecureRandom();

	/**
	 * Method used to construct value from tag and length
	 *
	 * @param pTagAndLength
	 *            tag and length value
	 * @return tag value in byte
	 */
	public static byte[] constructValue(final TagAndLength pTagAndLength, byte[] unp, String amt, TerminalVerificationResults tvr) {
		byte ret[] = new byte[pTagAndLength.getLength()];
		byte val[] = null;
		if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_QUALIFIERS) {
			TerminalTransactionQualifiers terminalQual = new TerminalTransactionQualifiers();
			terminalQual.setContactlessEMVmodeSupported(true);
			terminalQual.setOnlinePINsupported(true);
			terminalQual.setOnlineCryptogramRequired(true);
			val = terminalQual.getBytes();
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_COUNTRY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(CountryCodeEnum.NG.getNumeric()), pTagAndLength.getLength() * 2,
					"0"));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CURRENCY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(CurrencyEnum.NGN.getISOCodeNumeric()),
					pTagAndLength.getLength() * 2, "0"));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_DATE) {
			int year = Integer.parseInt(EmvParser.getMPOSMMDDHHMMSS('y'), 16);
			int month = Integer.parseInt(EmvParser.getMPOSMMDDHHMMSS('m'), 16);
			int date = Integer.parseInt(EmvParser.getMPOSMMDDHHMMSS('d'), 16);
			val = new byte[]{
					(byte) year,
					(byte) month,
					(byte) date
			};
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_TYPE) {
			val = new byte[] { (byte) TransactionTypeEnum.PURCHASE.getKey() };
		} else if (pTagAndLength.getTag() == EmvTags.AMOUNT_AUTHORISED_NUMERIC) {
			amt = padleft(amt + "00", 12, '0');
			byte[] amount = new byte[6];
			for (int i = 0; i < 11; i = i + 2) {
				amount[i / 2] = (byte) Integer.parseInt(amt.charAt(i) + "" + amt.charAt(i + 1), 16);
			}
			val = new byte[]{
					amount[0],
					amount[1],
					amount[2],
					amount[3],
					amount[4],
					amount[5]
			};
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TYPE) {
			val = new byte[] { 0x22 };
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_VERIFICATION_RESULTS) {
			byte[] TVR=tvr.toByteArray();
			val = new byte[]{
					TVR[0],
					TVR[1],
					TVR[2],
					TVR[3],
					TVR[4]
			};
		}  else if (pTagAndLength.getTag() == EmvTags.UNPREDICTABLE_NUMBER) {
			val =new byte[]{
					unp[0],
					unp[1],
					unp[2],
					unp[3]
			};
		}
		if (val != null) {
			System.arraycopy(val, 0, ret, 0, Math.min(val.length, ret.length));
		}
		return ret;
	}

	/**
	 * Private Constructor
	 */
	private EmvTerminal() {
	}

}
