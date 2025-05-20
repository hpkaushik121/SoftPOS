package com.aicortex.softpos.creditCardNfcReader.model;



import com.aicortex.softpos.creditCardNfcReader.enums.EmvCardScheme;
import com.aicortex.softpos.creditCardNfcReader.terminalData.CVMList;
import com.aicortex.softpos.creditCardNfcReader.terminalData.TransactionStatusInformation;

import java.util.Collection;
import java.util.List;

/*import id.paypro.nfc.creditCardNfcReader.enums.EmvCardScheme;
import id.paypro.nfc.creditCardNfcReader.terminalData.CVMList;
import id.paypro.nfc.creditCardNfcReader.terminalData.TransactionStatusInformation;*/

/**
 * Bean used to describe data in EMV card
 *
 */
public class EmvCard extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 736740432469989941L;

	/**
	 * Card AID
	 */
	private String aid;

	/**
	 * Card ICC Data
	 */
	private String iccData;

	/**
	 * Holder Lastname
	 */
	private String holderLastname;

	/**
	 * Holder Firstname
	 */
	private String holderFirstname;

	/**
	 * Card number
	 */
	private String cardNumber;

	/**
	 * Expiration date
	 */
	private String expireDate;

	/**
	 * Card type
	 */
	private EmvCardScheme type;

	/**
	 * Left PIN try
	 */
	private int leftPinTry;

	/**
	 * Application label
	 */
	private String applicationLabel;

	/**
	 * Cardholder Verification Method (CVM) List
	 */
	private com.aicortex.softpos.creditCardNfcReader.terminalData.CVMList CVMList;

	/**
	 * List of issued payment
	 */
	private List<EmvTransactionRecord> listTransactions;

	/**
	 * List of Atr description
	 */
	private Collection<String> atrDescription;

	/**
	 * Card services
	 */
	private Service service;

	/**
	 * Indicate if the nfc is locked on the card
	 */
	private boolean nfcLocked;
	private String track2;

	/**
	 * Application Primary Account Number (PAN) Sequence Number
	 */
	private String PANSeqNumber;

	private TransactionStatusInformation transactionStatusInformation=new TransactionStatusInformation();

	/**
	 *  @return Application Primary Account Number (PAN) Sequence Number
	 */
	public String getPANSeqNumber() {
		return PANSeqNumber;
	}
	/**
	 * @param PANSeqNumber
	 *
	 * set Application Primary Account Number (PAN) Sequence Number
	 */
	public void setPANSeqNumber(String PANSeqNumber) {
		this.PANSeqNumber = PANSeqNumber;
	}

	/**
	 * Method used to get the field aid
	 *
	 * @return the aid
	 */
	public String getAid() {
		return aid;
	}

	/**
	 * Setter for the field aid
	 *
	 * @param aid
	 *            the aid to set
	 */
	public void setAid(final String aid) {
		this.aid = aid;
	}

	/**
	 * Setter for the field ICCdata
	 *
	 * @param iccData
	 *            the iccData to set
	 */
	public void setIccData(String iccData) {
		this.iccData = iccData;
	}

	/**
	 * Method used to get the field holderLastname
	 *
	 * @return the holderLastname
	 */
	public String getHolderLastname() {
		return holderLastname;
	}

	/**
	 * Method used to get the field iccData
	 *
	 * @return the iccData
	 */
	public String getIccData() {
		return iccData;
	}

	/**
	 * Setter for the field holderLastname
	 *
	 * @param holderLastname
	 *            the holderLastname to set
	 */
	public void setHolderLastname(final String holderLastname) {
		this.holderLastname = holderLastname;
	}

	/**
	 * Method used to get the field holderFirstname
	 *
	 * @return the holderFirstname
	 */
	public String getHolderFirstname() {
		return holderFirstname;
	}

	/**
	 * Setter for the field holderFirstname
	 *
	 * @param holderFirstname
	 *            the holderFirstname to set
	 */
	public void setHolderFirstname(final String holderFirstname) {
		this.holderFirstname = holderFirstname;
	}

	/**
	 * Method used to get the field cardNumber
	 *
	 * @return the cardNumber
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * Setter for the field cardNumber
	 *
	 * @param cardNumber
	 *            the cardNumber to set
	 */
	public void setCardNumber(final String cardNumber) {
		this.cardNumber = cardNumber;
	}

	/**
	 * Method used to get the field expireDate
	 *
	 * @return the expireDate
	 */
	public String getExpireDate() {
		return expireDate;
	}

	/**
	 * Setter for the field expireDate
	 *
	 * @param expireDate
	 *            the expireDate to set
	 */
	public void setExpireDate(final String expireDate) {
		this.expireDate = expireDate;
	}
	public String getTrack2(){
		return track2;
	}

	public void setTrack2(String track2) {
		this.track2 = track2;
	}

	/**
	 * Method used to get the field listTransactions
	 *
	 * @return the listTransactions
	 */
	public List<EmvTransactionRecord> getListTransactions() {
		return listTransactions;
	}

	/**
	 * Setter for the field listTransactions
	 *
	 * @param listTransactions
	 *            the listTransactions to set
	 */
	public void setListTransactions(final List<EmvTransactionRecord> listTransactions) {
		this.listTransactions = listTransactions;
	}

	/**
	 * Method used to get the field type
	 *
	 * @return the type
	 */
	public EmvCardScheme getType() {
		return type;
	}

	/**
	 * Setter for the field type
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(final EmvCardScheme type) {
		this.type = type;
	}

	/**
	 * Method used to get the field applicationLabel
	 *
	 * @return the applicationLabel
	 */
	public String getApplicationLabel() {
		return applicationLabel;
	}

	/**
	 * Setter for the field applicationLabel
	 *
	 * @param applicationLabel
	 *            the applicationLabel to set
	 */
	public void setApplicationLabel(final String applicationLabel) {
		this.applicationLabel = applicationLabel;
	}

	@Override
	public boolean equals(final Object arg0) {
		return arg0 instanceof EmvCard && cardNumber != null && cardNumber.equals(((EmvCard) arg0).getCardNumber());
	}

	/**
	 * Method used to get the field leftPinTry
	 *
	 * @return the leftPinTry
	 */
	public int getLeftPinTry() {
		return leftPinTry;
	}

	/**
	 * Setter for the field leftPinTry
	 *
	 * @param leftPinTry
	 *            the leftPinTry to set
	 */
	public void setLeftPinTry(final int leftPinTry) {
		this.leftPinTry = leftPinTry;
	}

	/**
	 * Method used to get the field atrDescription
	 *
	 * @return the atrDescription
	 */
	public Collection<String> getAtrDescription() {
		return atrDescription;
	}

	/**
	 * Setter for the field atrDescription
	 *
	 * @param atrDescription
	 *            the atrDescription to set
	 */
	public void setAtrDescription(final Collection<String> atrDescription) {
		this.atrDescription = atrDescription;
	}

	/**
	 * Method used to get the field service
	 *
	 * @return the service
	 */
	public Service getService() {
		return service;
	}

	/**
	 * Setter for the field service
	 *
	 * @param service
	 *            the service to set
	 */
	public void setService(final Service service) {
		this.service = service;
	}

	/**
	 * Method used to get the field nfcLocked
	 *
	 * @return the nfcLocked
	 */
	public boolean isNfcLocked() {
		return nfcLocked;
	}

	/**
	 * Setter for the field nfcLocked
	 *
	 * @param nfcLocked
	 *            the nfcLocked to set
	 */
	public void setNfcLocked(final boolean nfcLocked) {
		this.nfcLocked = nfcLocked;
	}

	/**
	 * Method used to get the CVM List in Hex
	 *
	 * @return the CVMList
	 */
	public com.aicortex.softpos.creditCardNfcReader.terminalData.CVMList getCVMList() {
		return CVMList;
	}

	/**
	 * Setter for the field CVMList
	 *
	 * @param CVMList
	 *            the CVMList to set
	 */
	public void setCVMList(CVMList CVMList) {
		this.CVMList = CVMList;
	}

    public TransactionStatusInformation getTransactionStatusInformation() {
		return transactionStatusInformation;
    }

	public void setTransactionStatusInformation(TransactionStatusInformation transactionStatusInformation) {
		this.transactionStatusInformation = transactionStatusInformation;
	}
}
