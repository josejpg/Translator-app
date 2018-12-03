/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translator;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.stage.*;
import translator.model.*;
import translator.utils.*;

/**
 *
 * @author Jose J. Pardines Garcia
 */
public class FXMLDocumentController implements Initializable {
    
    private Label label;
    @FXML
    private MenuItem loadLanguages;
    @FXML
    private MenuItem exitProgram;
    @FXML
    private ListView<String> listSrc;
    @FXML
    private ListView<String> listTrg;
    @FXML
    private CheckBox autoDetectSrc;
    @FXML
    private Label sourceLanguage;
    @FXML
    private Label totalPairLanguages;
    @FXML
    private Label totalSentences;
    @FXML
    private TextArea textSrc;
    @FXML
    private TextArea textTranslated;
    List<Language> listLang = null;
    HashMap<String, HashMap<String, String>> allTranslates = new HashMap<>();
    ThreadPoolExecutor executor;
    Future<HashMap<String, HashMap<String, String>>> future;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        /**
         * Listener to close program
         */
        exitProgram.setOnAction( ( ActionEvent e ) -> {
            closeApp();
        });
        
        /**
         * Listener to open a FileChoose to select a language file
         * if it's a correct file get all info but if isn't
         * shows an error message
         */
        loadLanguages.setOnAction( ( ActionEvent e ) -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                fileChooser.setTitle("Open Resource File");
                Window stage = null;
                File file = fileChooser.showOpenDialog(stage);
                if( file != null){
                    listLang = FileUtils.readLanguages(Paths.get(file.getPath()));
                    setThreadPool();
                    setSrcListsLang();
                    setTrgListsLang();
                    setTotalPairLanguages();
                    setTotalSentences();
                            
                }else{
                    MessageUtils.showError("Error", "You must select one file.");
                }
            } catch ( NullPointerException ex ) {

                Logger
                    .getLogger( FileUtils.class.getName() )
                    .log( Level.SEVERE, null, ex );
                MessageUtils.showError( FileUtils.class.getName(), "NullPointerException:\n\n" + ex.getMessage() );
                stopThread();
                
            } catch ( Exception ex ) {

                Logger
                    .getLogger( FileUtils.class.getName() )
                    .log( Level.SEVERE, null, ex );
                MessageUtils.showError( FileUtils.class.getName(), "Exception:\n\n" + ex.getMessage() );
                stopThread();

            }
        });
        
        /**
         * Listener to write the translate words from source
         */
        textSrc.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
           // try {                
                /*if(future.isDone() &&
                    !future.isCancelled() &&
                    future.get() != null){
                    allTranslates = future.get();
                }*/
               if (!textSrc.textProperty().get().isEmpty()){
                   /*String[] allSrcSentences = newValue.trim().split( "\n" );
                    for( String singleSentence: allSrcSentences ){
                        if( !singleSentence.isEmpty() &&
                             allTranslates != null &&
                             allTranslates.size() > 0 &&
                             allTranslates.get(listTrg.getSelectionModel().getSelectedItem()).containsKey(singleSentence)
                         ){
                            tmpHashMap = allTranslates.get(listTrg.getSelectionModel().getSelectedItem());
                            textTranslated.setText(tmpHashMap.get(singleSentence));
                        }
                    }*/
                    for (Language dataLang: listLang){
                        if (dataLang.getHashSentences().containsKey(textSrc.getText()) ){

                            if (dataLang.getTrgLang().equals(listTrg.getSelectionModel().getSelectedItem()))
                            {
                                textTranslated.setText(dataLang.getHashSentences().get(textSrc.getText()));
                            }
                        }
                    }
               }
               else
               {
                   textTranslated.setText("");
               }
           /* } catch (InterruptedException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                MessageUtils.showError( FileUtils.class.getName(), "textSrc.textProperty().addListener InterruptedException:\n\n" + ex.getMessage() );
                stopThread();
            } catch (ExecutionException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                MessageUtils.showError( FileUtils.class.getName(), "textSrc.textProperty().addListener ExecutionException:\n\n" + ex.getMessage() );
                stopThread();
            }*/
        
        });
        
        /**
         * Listener to change text property with the new source language
         * and set news targets
         */
        listSrc.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            sourceLanguage.textProperty().set( newValue );
            setTrgListsLang();
        });
        
        /*listTrg.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            System.out.println("ListView selection changed from oldValue = "
                    + oldValue + " to newValue = " + newValue);
        });*/
    }
    
    /**
     * Close app
     */
    private void closeApp(){
        Platform.exit();
        stopThread();
    }
    /**
     * Stop thread pool
     */
    private void stopThread(){
        future.cancel(true);
        executor.shutdownNow();
    }
    /**
     * Set the targets language for the source selected
     */
    private void setTrgListsLang(){
        listTrg.getItems().clear();
        for(Language dataLang : listLang) {
            if(!dataLang.getTrgLang().equals( listSrc.getSelectionModel().getSelectedItem() ) ){       
                if(listTrg.getItems().size() > 0 ){       
                    if(!listTrg.getItems().contains(dataLang.getTrgLang()) ){
                       listTrg.getItems().add(dataLang.getTrgLang());
                    }
                }else{
                    listTrg.getItems().add(dataLang.getTrgLang());
                }
            }
        }
    }
    /**
     * Set the source languages
     */
    private void setSrcListsLang(){
        listSrc.getItems().clear();
        for(Language dataLang : listLang) {
                           
            if(!listSrc.getItems().contains(dataLang.getSrcLang())){
                listSrc.getItems().add(dataLang.getSrcLang());
            }
        }
    }
    /**
     * Write a text with total language
     */
    private void setTotalPairLanguages(){
        if (listLang.size() > 0){
            totalPairLanguages.textProperty().set( Integer.toString( listLang.size() ) );
        }
        else
        {
            totalPairLanguages.textProperty().set("0");
        }
    }
    /**
     * Write a text with total sentences
     */
    private void setTotalSentences(){
        if (listLang.size() > 0){
            int countTotalHashMap = 0;
            for(Language lang : listLang){
                countTotalHashMap += lang.getHashSentences().size();
            }
            totalSentences.textProperty().set( Integer.toString( countTotalHashMap ) );
        }
        else
        {
            totalSentences.textProperty().set( "0" );
        }
    }
    
    /**
     * Throw a thread pool to scann all tralation options
     */
    private void setThreadPool(){
        executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        future = executor.submit((Callable<HashMap<String, HashMap<String, String>>>) new ThreadPool(listLang));
        int count = 1;
        while(!future.isDone()){
            System.out.println( "Waiting.. " + count + "sec" );
            count++;
        }
    }
}
