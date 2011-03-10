package org.tomac.tools.converter.nordic;


public class NordicUtils {

	public static boolean isTagWithLength(String tag) {
		// Tag		Max Length
		if (getTagLength(tag).length()>0) return true;
		return false;
	}

	public static String getTagLength(String tag) {
		if (tag.equals("11")) return "20";
		if (tag.equals("37")) return "32";
		if (tag.equals("41")) return "20";
		if (tag.equals("49")) return "12";
		if (tag.equals("50")) return "6";
		if (tag.equals("55")) return "6";
		if (tag.equals("56")) return "12";
		if (tag.equals("57")) return "20";
		if (tag.equals("115")) return "12";
		if (tag.equals("116")) return "6";
		if (tag.equals("128")) return "12";
		if (tag.equals("129")) return "6";
		if (tag.equals("440")) return "12";
		if (tag.equals("439")) return "4";
		if (tag.equals("571")) return "20";
		if (tag.equals("880")) return "10";
		if (tag.equals("5149")) return "10";
		if (tag.equals("5815")) return "3";
		if (tag.equals("6204")) return "20";
		if (tag.equals("6205")) return "20";
		if (tag.equals("6209")) return "15";
		if (tag.equals("9292")) return "4";
		if (tag.equals("9861")) return "10";
		return "";
	}

	public static String getMessageSubMsgType(String name) {
		// Tag 150, ExecType
		if (name.equals("AcceptedCancelReplace")) return "5";
		if (name.equals("AcceptedCancel")) return "4";
		if (name.equals("ExecutionReportFill")) return "1"; //"2"
		if (name.equals("ExecutionRestatement")) return "D";
		if (name.equals("OrderAcknowledgement")) return "0";
		if (name.equals("OrderReject")) return "8";
		if (name.equals("PendingCancel")) return "6";
		if (name.equals("BusinessReject")) return "I";
		// and Tag 58 Text starts with "!REJ"
		if (name.equals("ApplicationReject")) return "AI";

		// Tag 856, TradeReportType
		if (name.equals("TradeReportEntry")) return "T0";
		if (name.equals("TradeReportCancel")) return "T6";
		if (name.equals("LockedinTradeBreak")) return "T7";
		if (name.equals("TradeEntryNotificationtoEnteringFirm")) return "t0";
		if (name.equals("EntryNotificationtoAllegedFirm")) return "t1";
		if (name.equals("LockedinNotification")) return "t2";
		if (name.equals("BreakNotification")) return "t7";
		if (name.equals("CancelNotification")) return "t6";	
		
		// 434 CxlRejResponseTo
		if (name.equals("OrderCancelReject")) return "1";
		if (name.equals("RejectedOrderCancelReplace")) return "2";	
		
		return "";
	}

	public static boolean isMessageWithSubMsgType(String name) {
		if (getMessageSubMsgType(name).length() > 0) return true; 		
		return false;
	}

}
