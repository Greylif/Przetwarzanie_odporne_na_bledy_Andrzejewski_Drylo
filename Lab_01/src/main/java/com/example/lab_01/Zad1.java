package com.example.lab_01;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class Zad1 {

  private static final Logger LOGGER = Logger.getLogger(Zad1.class.getName());
  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int KEY_SIZE = 128;

  public static class Firma implements Serializable {
    private static final long serialVersionUID = 2022L;

    private transient String nip;
    private String nazwa;
    private String address;
    private LocalDate datazalozenia;
    private List<Department> departments;

    public Firma(String nip, String nazwa, String address, LocalDate datazalozenia) {
      this.nip = nip;
      this.nazwa = nazwa;
      this.address = address;
      this.datazalozenia = datazalozenia;
      this.departments = Arrays.asList(
          new Department("IT", 12),
          new Department("HR", 3),
          new Department("Finanse", 5)
      );
    }

    @Override
    public String toString() {
      return "Firma " + "nip=" + nip + ", " + "nazwa=" + nazwa + ", " +
          "address=" + address + ", " + "datazalozenia=" + datazalozenia;
    }
  }

  public static class Department implements Serializable {
    private static final long serialVersionUID = 2023L;
    private String nazwa;
    private int pracownicy;

    public Department(String nazwa, int pracownicy) {
      this.nazwa = nazwa;
      this.pracownicy = pracownicy;
    }

    @Override
    public String toString() {
      return "Department nazwa=" + nazwa + ", " + "pracownicy=" + pracownicy;
    }
  }

  private static class SerializableFirmaWrapper implements Serializable {
    private static final long serialVersionUID = 2024L;
    private String encryptedNip;
    private String nazwa;
    private String address;
    private LocalDate datazalozenia;
    private List<Department> departments;
    private String encodedKey;

    public SerializableFirmaWrapper(String encryptedNip, String nazwa, String address,
        LocalDate datazalozenia, List<Department> departments, String encodedKey) {
      this.encryptedNip = encryptedNip;
      this.nazwa = nazwa;
      this.address = address;
      this.datazalozenia = datazalozenia;
      this.departments = departments;
      this.encodedKey = encodedKey;
    }
  }

  private static SecretKey generateKey() throws KeyGenerationException {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
      keyGenerator.init(KEY_SIZE);
      return keyGenerator.generateKey();
    } catch (Exception e) {
      throw new KeyGenerationException("Błąd podczas generowania klucza AES", e);
    }
  }


  private static String encrypt(String plaintext, Key key) throws EncryptionException {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(ciphertext);
    } catch (Exception e) {
      throw new EncryptionException("Błąd podczas szyfrowania", e);
    }
  }

  private static String decrypt(String ciphertext, Key key) throws DecryptionException {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key);
      byte[] decryptedText = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
      return new String(decryptedText, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new DecryptionException("Błąd podczas deszyfrowania", e);
    }
  }

  private static void serializeFirmaToFile(Firma firma, String pathFile)
      throws SerializationException, EncryptionException, KeyGenerationException {
    try {
      SecretKey key = generateKey();
      String encryptedNip = null;
      if (firma.nip != null) {
        encryptedNip = encrypt(firma.nip, key);
      }

      String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());

      SerializableFirmaWrapper wrapper = new SerializableFirmaWrapper(
          encryptedNip, firma.nazwa, firma.address, firma.datazalozenia,
          firma.departments, encodedKey
      );

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ObjectOutputStream oos = new ObjectOutputStream(baos)) {

        oos.writeObject(wrapper);
        String encodedData = Base64.getEncoder().encodeToString(baos.toByteArray());
        Files.write(Paths.get(pathFile), encodedData.getBytes(StandardCharsets.UTF_8));

        LOGGER.log(java.util.logging.Level.INFO,
            "Zserializowano i zaszyfrowano dane do: {0}", pathFile);
      }
    } catch (IOException | KeyGenerationException | EncryptionException e) {
      throw new SerializationException("Błąd podczas serializacji firmy", e);
    } catch (Exception e) {
      throw new SerializationException("Nieoczekiwany błąd podczas serializacji firmy", e);
    }
  }

  private static Firma deSerializeFirmaFromFile(String pathFile)
      throws DeserializationException, DecryptionException {
    try {
      String encodedData = Files.readAllLines(Paths.get(pathFile)).get(0);
      byte[] data = Base64.getDecoder().decode(encodedData);
      try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
        SerializableFirmaWrapper wrapper = (SerializableFirmaWrapper) ois.readObject();

        byte[] keyBytes = Base64.getDecoder().decode(wrapper.encodedKey);
        SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);

        String nip = null;
        if (wrapper.encryptedNip != null) {
          nip = decrypt(wrapper.encryptedNip, key);
        }

        Firma firma = new Firma(nip, wrapper.nazwa, wrapper.address, wrapper.datazalozenia);
        firma.departments = wrapper.departments;
        return firma;
      }
    } catch (IOException | ClassNotFoundException | DecryptionException e) {
      throw new DeserializationException("Błąd podczas deserializacji firmy", e);
    } catch (Exception e) {
      throw new DeserializationException("Nieoczekiwany błąd podczas deserializacji", e);
    }
  }

  public static void main(String[] args) {
    try {
      final String pathToFile = "Zad1.txt";
      Firma firma = new Firma("9876543210", "Politechnika Swietokrzyska",
          "al. Tysiaclecia Panstwa Polskiego 7, Warszawa", LocalDate.of(2012, 5, 15));

      serializeFirmaToFile(firma, pathToFile);
      Firma loadedFirma = deSerializeFirmaFromFile(pathToFile);

      LOGGER.info("Odczytano obiekt z pliku:");
      String tmp = loadedFirma.toString();
      LOGGER.info(tmp);
      for (Department dep : loadedFirma.departments) {
        String tmp2 = dep.toString();
        LOGGER.info(tmp2);
      }
    } catch (EncryptionException | DecryptionException e) {
      LOGGER.severe("Błąd szyfrowania lub deszyfrowania: " + e.getMessage());
    } catch (Exception e) {
      LOGGER.severe("Wystąpił nieoczekiwany błąd: " + e.getMessage());
    }
  }
}
