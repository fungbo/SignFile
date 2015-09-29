import org.apache.commons.io.IOUtils;

import java.io.*;

public class Signer {
    public static final String PATH = "sign";
    public static final String PRIVATE_KEY_PEM = "key.pem";
    public static final String PRIVATE_KEY_DER = "key.pk8";

    public void sign(InputStream privateKeyPem, String fileURL) throws IOException, InterruptedException {
        createFolder();
        savePrivateKey(privateKeyPem);
        convertPrivateKeyToDerFormat();
        doSign(fileURL);
    }

    private void doSign(String fileURL) {
        try {
            String command = String.format("java -jar %s %s %s %s %s",
                    getSignJarPath(), getCertificatePath(), getPrivateKeyPath(),
                    fileURL, "signed.jar");
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void convertPrivateKeyToDerFormat() throws IOException, InterruptedException {
        File file = new File(getPrivateKeyPemPath());
        if (!file.exists()) {
            throw new FileNotFoundException("Can not find private key file");
        }

        String command = String.format("openssl pkcs8 -topk8 -outform DER -in %s -inform PEM -out %s -nocrypt",
                getPrivateKeyPemPath(), getPrivateKeyDerPath());
        Process exec = Runtime.getRuntime().exec(command);
        exec.waitFor();
    }

    private void savePrivateKey(InputStream privateKeyPem) throws IOException {
        FileOutputStream fos = new FileOutputStream(getPrivateKeyPemPath());
        fos.write(IOUtils.toByteArray(privateKeyPem));
        fos.flush();
    }

    private boolean createFolder() {
        File file = new File(PATH);
        if (file.exists()) {
            file.delete();
        }

        return file.mkdirs();
    }

    private String getPrivateKeyPemPath() {
        return PATH + "/" + PRIVATE_KEY_PEM;
    }

    private String getPrivateKeyDerPath() {
        return PATH + "/" + PRIVATE_KEY_DER;
    }

    private String getSignJarPath() {
        return Thread.currentThread().getContextClassLoader().getResource("signapk.jar").getPath();
    }

    private String getCertificatePath() {
        return Thread.currentThread().getContextClassLoader().getResource("certificate.pem").getPath();
    }

    private String getPrivateKeyPath() {
        return new File(getPrivateKeyDerPath()).getAbsolutePath();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PRIVATE_KEY_PEM);
        Signer signer = new Signer();
        String filePath = Thread.currentThread().getContextClassLoader().getResource("springboot-web.jar").getPath();
        signer.sign(inputStream, filePath);
    }
}
