package ppe.abitbol.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import java.util.Set;
import java.util.UUID;
import java.lang.Thread;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PPE extends Activity
{
    /* BT constants. */
    private static final int SUCCEEDED = 1, FAILED = 0, MESSAGE_READ = 2;
    private static final String DEVICE_NAME = "PPEFermat"; // Le nom du périphérique bluetooth
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FC"); // Sert à identifier l'application lors de la connexion bluetooth
	private BluetoothAdapter bluetoothAdapter;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;

    /* Widgets */
    private TextView connectingText;
    private Button runToggle;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        set_main();
        
		// On récupère l'accès au bluetooth
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!bluetoothAdapter.isEnabled()) { // On l'active si ce n'est pas déjà fait
			bluetoothAdapter.enable();
			while(!bluetoothAdapter.isEnabled()) { }
		}

    }

	public void launchConnexion() {
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices(); // On récupère la liste des périphériques bluetooth
		for(BluetoothDevice device : pairedDevices) { // On cherche s'il y en a un qui correspond à celui qu'on cherche (DEVICE_NAME)
			if(device.getName().equals(DEVICE_NAME)) {
				connectingText.setText("Connexion en cours..."); // On affiche la tentative de connexion
				connectThread = new ConnectThread(device); // On créé un thread de connexion
				connectThread.start(); // On le lance
			}
		}
	}
	

    /** Display the main layout. */
    public void set_main()
    {
        setContentView(R.layout.main);
    }

    /** Called when the connect button is clicked. */
    public void launch_connect(View view)
    {
        setContentView(R.layout.connecting);
        connectingText = (TextView)findViewById(R.id.connectingText);
        connecting();
    }

    /** Called when the mainQuit button is clicked. */
    public void main_quit(View view)
    {
        finish();
    }

    /** Connect to the robot. */
    public void connecting()
    {
		launchConnexion();
    }

    /** Launch the layout when connected. */
    public void run()
    {
        setContentView(R.layout.running);
        runToggle = (Button)findViewById(R.id.runToggle);
    }

    /** Called when clicked on runToggle button. */
    public void run_toggle(View view)
    {
        /* TODO Send orders to the remote robot. */
        if(runToggle.getText() == "Stop") {
            runToggle.setText("Follow");
        }
        else {
            runToggle.setText("Stop");
        }
    }

    /** Called when clicked on runQuit button. */
    public void run_quit(View view)
    {
        /* TODO close the connction. */
        finish();
    }

    // Handler de connection
    
	private Handler connexionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case SUCCEEDED : // Si ça réussit
                run();
				break;
			case FAILED : // Si ça échoue
				connectingText.setText("Connexion échouée"); // On l'indique
				break;
			case MESSAGE_READ : // Si un message est reçu
                // TODO
				byte[] buffer = (byte[])msg.obj;
				String messageRead = new String(buffer, 0, msg.arg1);
				break;
			}
		}
	};

    // Thread de connexion

    private class ConnectThread extends Thread { // Thread de connexion
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID); // On crée le socket bluetooth
            } catch (IOException e) { }

            mmSocket = tmp;
        }

        public void run() { // Au lancement du thread
            try {
                mmSocket.connect(); // On lance la connexion
            } catch (IOException connectException) { // En cas d'erreur
                try {
                    mmSocket.close(); // On ferme le socket
                } catch (IOException closeException) { }
                connexionHandler.sendMessage(connexionHandler.obtainMessage(FAILED)); // On envoie le message d'erreur	
                return;
            }
            connectedThread = new ConnectedThread(mmSocket); // On crée un thread de contrôle avec le socket bluetooth
            connectedThread.start(); // On le lance
            connexionHandler.sendMessage(connexionHandler.obtainMessage(SUCCEEDED)); // On envoie le message de réussite
        }

        public void cancel() { // À la fermeture de thread
            try {
                mmSocket.close(); // On ferme le socket
            } catch (IOException e) { }
        }
    }

    // Thread de gestion de la connexion

    private class ConnectedThread extends Thread { // Thread de contrôle
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream(); // Ouvre un flux entrant sur le socket passé en paramètre
                tmpOut = socket.getOutputStream(); // Ouvre un flux sortant sur le socket passé en paramètre
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    connexionHandler.sendMessage(connexionHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer));
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) { // Sert à envoyer une commande brute, sans traitement
            try {
                mmOutStream.write(message.getBytes());
            } catch (IOException e) { }
        }

        public void cancel() { // À la fin du thread
            try {
                mmSocket.close(); // On ferme le socket
            } catch (IOException e) { }
        }
    }

}
