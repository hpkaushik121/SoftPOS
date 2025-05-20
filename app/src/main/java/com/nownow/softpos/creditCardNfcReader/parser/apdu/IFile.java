package com.aicortex.softpos.creditCardNfcReader.parser.apdu;

import com.aicortex.softpos.creditCardNfcReader.iso7816emv.TagAndLength;

import java.util.List;

//import id.paypro.nfc.creditCardNfcReader.iso7816emv.TagAndLength;

/**
 * Interface for File to parse
 */
public interface IFile {

	/**
	 * Method to parse byte data
	 * 
	 * @param pData
	 *            byte to parse
	 * @param pList
	 *            Tag and length
	 */
	void parse(final byte[] pData, final List<TagAndLength> pList);

}
