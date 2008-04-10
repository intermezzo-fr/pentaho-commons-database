package org.pentaho.test.ui.database;

import java.io.InputStream;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.containers.XulWindow;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class DatabaseDialogHarness {

  DatabaseMeta database = null;

  public static void main(String[] args) {

    DatabaseDialogHarness harness = new DatabaseDialogHarness();
    
    try {
      InputStream in = DatabaseDialogHarness.class.getClassLoader()
            .getResourceAsStream("org/pentaho/ui/database/databasedialog.xul");
      if (in == null) {
        System.out.println("Invalid Input");
        return;
      }
      
      SAXReader rdr = new SAXReader();
      final Document doc = rdr.read(in);
      
      harness.showDialog(doc);
      
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  private void showDialog(final Document doc){

    XulDomContainer container = null;
    try {
      container = new SwtXulLoader().loadXul(doc);
      if (database != null){
        container.getEventHandler("dataHandler").setData(database);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } 
    XulWindow dialog = (XulWindow) container.getDocumentRoot().getRootElement();
    Shell s = (Shell)dialog.getManagedObject();
    s.setText("New Database Connection");
    dialog.open();

    try {
      database = (DatabaseMeta) container.getEventHandler("dataHandler").getData();
    } catch (Exception e) {
      e.printStackTrace();
    }

    String message = DatabaseDialogHarness.setMessage(database);
    Shell shell = new Shell(SWT.DIALOG_TRIM);
    shell.setLayout(new RowLayout());
    Label label = new Label(shell, SWT.NONE);
    label.setText(message);
    Button button = new Button(shell, SWT.NONE);
    button.setText("Edit Database ...");

    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        try {
          showDialog(doc);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    shell.pack();
    shell.open();

    while (!shell.isDisposed()) {
      if (!shell.getDisplay().readAndDispatch()) {
        shell.getDisplay().sleep();
      }
    }
  }

  private static String setMessage(DatabaseMeta database) {
    String message = "";
    if (database != null) {
      String carriageReturn = System.getProperty("line.separator");
      try {
        message = "Name: ".concat(database.getName()).concat(carriageReturn)
        .concat("Database Name: ").concat(database.getDatabaseName()).concat(carriageReturn)
        .concat("Host Name: ").concat(database.getHostname()).concat(carriageReturn)
        .concat("Port Number: ").concat(database.getDatabasePortNumberString()).concat(carriageReturn)
                .concat("User Name: ").concat(database.getUsername()).concat(carriageReturn)
                .concat("Password: ").concat(database.getPassword()).concat(carriageReturn)
                .concat("Driver Class: ").concat(database.getDriverClass()).concat(carriageReturn)
                .concat("URL: ").concat(database.getURL()).concat(carriageReturn);
        
        
        Iterator<String> keys = database.getExtraOptions().keySet().iterator();
        message = message.concat(carriageReturn).concat("Option Parameters:").concat(carriageReturn);
        while (keys.hasNext()){
            String parameter = keys.next();
            String value = database.getExtraOptions().get(parameter);
            message = message.concat(carriageReturn).concat(parameter).concat(": ").concat(value).concat(carriageReturn);
        }
        
        message = message.concat(carriageReturn).concat("SQL: ")
        .concat(database.getConnectSQL()!=null ? database.getConnectSQL() : "").concat(carriageReturn)
        .concat("Quote Identifiers: ").concat(Boolean.toString(database.isQuoteAllFields())).concat(carriageReturn)
        .concat("Upper Case Identifiers: ").concat(Boolean.toString(database.isForcingIdentifiersToUpperCase())).concat(carriageReturn)
        .concat("Lower Case Identifiers: ").concat(Boolean.toString(database.isForcingIdentifiersToLowerCase())).concat(carriageReturn);
        
        message = message.concat(carriageReturn).concat("Is Partitioned: ")
        .concat(Boolean.toString(database.isPartitioned())).concat(carriageReturn);
        
        if (database.isPartitioned()){
          PartitionDatabaseMeta[] partitions = database.getPartitioningInformation();
          if (partitions != null){
            for (int i = 0; i < partitions.length; i++) {
              PartitionDatabaseMeta pdm = partitions[i];
              message = message.concat(carriageReturn).concat(Integer.toString(i)).concat(". ID: ")
              .concat(pdm.getPartitionId()).concat(", Host: ")
              .concat(pdm.getHostname()).concat(", Db: ")
              .concat(pdm.getDatabaseName()).concat(", Port: ")
              .concat(pdm.getPort()).concat(", User: ")
              .concat(pdm.getUsername()).concat(", Pass: ")
              .concat(pdm.getPassword()).concat(carriageReturn);
            }
          }
        }
        Iterator <Object> poolKeys  = database.getConnectionPoolingProperties().keySet().iterator();
        message = message.concat(carriageReturn).concat("Pooling Parameters:").concat(carriageReturn);
        while (poolKeys.hasNext()){
            String parameter = (String)poolKeys.next();
            String value = database.getConnectionPoolingProperties().getProperty(parameter);
            message = message.concat(carriageReturn).concat(parameter).concat(": ").concat(value).concat(carriageReturn);
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return message;

  }

}
