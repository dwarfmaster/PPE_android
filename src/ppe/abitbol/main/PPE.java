package ppe.abitbol.main;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class PPE extends Activity
{
    /* Widgets */
    private TextView connectingText;
    private Button runToggle;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        set_main();
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
        /* TODO connect */
        if(false /* If connecion has failed. */) {
            connectingText.setText("connection failed.");
            set_main();
        }
        connectingText.setText("connected.");
        run();
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
}
