package org.example;

import java.util.Random;

public class Main {

  public static class HammingCode {

    public static String encode(String dataBits) {
      int[] d = new int[4];
      for (int i = 0; i < 4; i++)
        d[i] = dataBits.charAt(i) - '0';

      int p1 = d[0] ^ d[1] ^ d[3];
      int p2 = d[0] ^ d[2] ^ d[3];
      int p4 = d[1] ^ d[2] ^ d[3];

      return "" + p1 + p2 + d[0] + p4 + d[1] + d[2] + d[3];
    }

    public static String[] decode(String codeWord) {
      int[] c = new int[7];
      for (int i = 0; i < 7; i++)
        c[i] = codeWord.charAt(i) - '0';

      int s1 = c[0] ^ c[2] ^ c[4] ^ c[6];
      int s2 = c[1] ^ c[2] ^ c[5] ^ c[6];
      int s4 = c[3] ^ c[4] ^ c[5] ^ c[6];
      int errorPos = s1 + 2 * s2 + 4 * s4;

      if (errorPos > 0)
        c[errorPos - 1] ^= 1;

      String data = "" + c[2] + c[4] + c[5] + c[6];
      return new String[]{data, String.valueOf(errorPos)};
    }
  }

  public static class CRCCode {

    public static String crcCalculate(String data, String polynomial) {
      int r = polynomial.length() - 1;
      char[] padded = (data + "0".repeat(r)).toCharArray();
      for (int i = 0; i < data.length(); i++) {
        if (padded[i] == '1') {
          for (int j = 0; j < polynomial.length(); j++) {
            int xorResult = (padded[i + j] - '0') ^
                (polynomial.charAt(j) - '0');
            padded[i + j] = (char) (xorResult + '0');
          }
        }
      }
      return new String(padded, padded.length - r, r);
    }

    public static boolean crcVerify(String dataWithCrc, String polynomial) {
      int r = polynomial.length() - 1;
      char[] padded = dataWithCrc.toCharArray();
      for (int i = 0; i < dataWithCrc.length() - r; i++) {
        if (padded[i] == '1') {
          for (int j = 0; j < polynomial.length(); j++) {
            int xorResult = (padded[i + j] - '0') ^
                (polynomial.charAt(j) - '0');
            padded[i + j] = (char) (xorResult + '0');
          }
        }
      }
      for (int i = padded.length - r; i < padded.length; i++) {
        if (padded[i] != '0') {
          return false;
        }
      }
      return true;
    }
  }

  public static void main(String[] args) {

    Random rand = new Random();

    String data = String.format("%4s", Integer.toBinaryString(rand.nextInt(16))).replace(' ', '0');

    System.out.println("\nHAMMING(7,4)");
    System.out.println("Losowe dane: " + data);

    String encodedH = HammingCode.encode(data);
    System.out.println("Zakodowane:  " + encodedH);

    int errorIdx = rand.nextInt(7);
    char[] corruptedArray = encodedH.toCharArray();
    corruptedArray[errorIdx] = corruptedArray[errorIdx] == '0' ? '1' : '0';
    String corrupted = new String(corruptedArray);

    System.out.println("Uszkodzone (błąd na poz " + (errorIdx + 1) + "): " + corrupted);

    String[] result = HammingCode.decode(corrupted);
    String decoded = result[0];
    int pos = Integer.parseInt(result[1]);
    System.out.println("Naprawione: " + decoded + ", wykryty błąd na pozycji: " + pos);

    assert data.equals(decoded) : "Błąd dekodowania!";
    System.out.println("Test OK");

    System.out.println("\nCRC");
    int length = 8 + rand.nextInt(13);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++)
      sb.append(rand.nextBoolean() ? '1' : '0');
    String dataBits = sb.toString();

    String poly = "1011";

    System.out.println("Dane: " + dataBits);
    System.out.println("Wielomian: " + poly);
    String crc = CRCCode.crcCalculate(dataBits, poly);
    System.out.println("Obliczone CRC: " + crc);
    String transmittedFrame = dataBits + crc;
    System.out.println("Ramka do wysłania: " + transmittedFrame);
    boolean isValid = CRCCode.crcVerify(transmittedFrame, poly);
    System.out.println("Weryfikacja oryginalnej ramki: " + (isValid ? "OK" : "BŁĄD"));
    assert isValid : "Weryfikacja powinna być poprawna!";
    errorIdx = rand.nextInt(transmittedFrame.length());
    char[] corruptedList = transmittedFrame.toCharArray();
    corruptedList[errorIdx] = (corruptedList[errorIdx] == '0') ? '1' : '0';
    String corruptedFrame = new String(corruptedList);
    System.out.println("Ramka uszkodzona (błąd na idx " + errorIdx + "): " + corruptedFrame);
    boolean isValidCorrupted = CRCCode.crcVerify(corruptedFrame, poly);
    System.out.println("Weryfikacja uszkodzonej ramki: " + (isValidCorrupted ? "OK" : "BŁĄD WYKRYTY"));
    assert !isValidCorrupted : "Błąd powinien b`yć wykryty!";
    System.out.println("Test CRC zakończony pomyślnie.");


    System.out.println("Hamming (7,4) wykrywa i NAPRAWIA 1 błąd.");
    System.out.println("CRC wykrywa błędy, ale ich nie naprawia.");
    System.out.println("Hamming działa dla 4-bitowych wiadomości.");
    System.out.println("CRC działa dla dowolnej długości.");

  }
}
