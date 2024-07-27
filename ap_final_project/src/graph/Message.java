package graph;

import java.util.Date;


public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(String messageString) {
        this.asText = messageString;
        this.data = convertStringToBytes(messageString);
        this.asDouble = convertStringToDouble(messageString);
        this.date = new Date();
    }

    public Message(byte[] messageBytes) {
        this(convertBytesToString(messageBytes));
    }

    public Message(double messageDouble) {
        this(convertDoubleToString(messageDouble));
    }

    public String toString() {
        return String.format("Message: text='%s', double=%f, date=%s", asText, asDouble, date);
    }


    private static String convertDoubleToString(double messageDouble) {
        return Double.toString(messageDouble);
    }

    private static String convertBytesToString(byte[] messageBytes) {
        return new String(messageBytes);
    }

    private static byte[] convertStringToBytes(String messageString) {
        return messageString.getBytes();
    }

    private static double convertStringToDouble(String messageString) {
        try {
            return Double.parseDouble(messageString);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

}