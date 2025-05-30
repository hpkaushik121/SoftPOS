package com.aicortex.softpos.creditCardNfcReader.iso7816emv;


import com.aicortex.softpos.creditCardNfcReader.enums.TagTypeEnum;
import com.aicortex.softpos.creditCardNfcReader.enums.TagValueTypeEnum;

/*import id.paypro.nfc.creditCardNfcReader.enums.TagTypeEnum;
import id.paypro.nfc.creditCardNfcReader.enums.TagValueTypeEnum;*/

public interface ITag {

	enum Class {
		UNIVERSAL, APPLICATION, CONTEXT_SPECIFIC, PRIVATE
	}

	boolean isConstructed();

	byte[] getTagBytes();

	String getName();

	String getDescription();

	TagTypeEnum getType();

	TagValueTypeEnum getTagValueType();

	Class getTagClass();

	int getNumTagBytes();

}
