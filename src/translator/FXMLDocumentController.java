/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translator;

import java.io.*;
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
    ThreadPoolExecutor executor;
    List<Future<String>> future;
    List<Callable<String>> callable;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        /**
         * Init in disable mode
         */
        autoDetectSrc.disableProperty().set( true );
        autoDetectSrc.setOnAction( ( ActionEvent e ) -> {
            if (!autoDetectSrc.isSelected()) {
                listSrc.disableProperty().set( false );
            } else {
                listSrc.disableProperty().set( true );
                setTrgListsLang();
                setSrcListsLang();
            }
        });
        
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
                    autoDetectSrc.disableProperty().set( false );
                    listLang = FileUtils.readLanguages(Paths.get(file.getPath()));
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
                
            } catch ( Exception ex ) {

                Logger
                    .getLogger( FileUtils.class.getName() )
                    .log( Level.SEVERE, null, ex );
                MessageUtils.showError( FileUtils.class.getName(), "Exception:\n\n" + ex.getMessage() );

            }
        });
        
        /**
         * Listener to write the translate words from source
         */
        textSrc.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if ( !textSrc.textProperty().get().isEmpty() ){
                if ( !autoDetectSrc.isSelected() ) {
                    listLang.forEach( (dataLang) -> {
                        if( dataLang.getSrcLang().equals( listSrc.getSelectionModel().getSelectedItem() ) &&
                            dataLang.getTrgLang().equals( listTrg.getSelectionModel().getSelectedItem() )
                        ){
                           setThreadPool( dataLang );
                        }
                    });
                } else {
                    listLang.forEach( (dataLang) -> {
                        if( dataLang.getTrgLang().equals( listTrg.getSelectionModel().getSelectedItem() )
                        ){
                           setThreadPool( dataLang );
                        }
                    });
                }
            } else {
                textTranslated.setText( "" );
            }
        
        });
        
        /**
         * Listener to change text property with the new source language
         * and set news targets
         */
        listSrc.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            sourceLanguage.textProperty().set( newValue );
            setTrgListsLang();
        });
        
        /**
         * Listener to change text property with the new target language
         * and set news targets
         */
        listTrg.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if( newValue != null ){
                if ( !autoDetectSrc.isSelected() ) {
                    listLang.forEach( (dataLang) -> {
                        if( dataLang.getSrcLang().equals( listSrc.getSelectionModel().getSelectedItem() ) &&
                            dataLang.getTrgLang().equals( newValue )
                        ){
                           setThreadPool( dataLang );
                        }
                    });
                } else {
                    listLang.forEach( (dataLang) -> {
                        if( dataLang.getTrgLang().equals( newValue ) ){
                           setThreadPool( dataLang );
                        }
                    });
                }
            } else {
                textTranslated.setText( "" );
            }
        });
    }
    
    /**
     * Close app
     */
    private void closeApp(){
        Platform.exit();
    }
    
    /**
     * Set the targets language for the source selected
     */
    private void setTrgListsLang(){
        listTrg.getItems().clear();
        for( Language dataLang : listLang ) {
            if( !dataLang.getTrgLang().equals( listSrc.getSelectionModel().getSelectedItem() ) ){       
                if( listTrg.getItems().size() > 0 ){       
                    if( !listTrg.getItems().contains( dataLang.getTrgLang() ) ){
                       listTrg.getItems().add( dataLang.getTrgLang() );
                    }
                }else{
                    listTrg.getItems().add( dataLang.getTrgLang() );
                }
            }
        }
    }
    /**
     * Set the source languages
     */
    private void setSrcListsLang(){
        listSrc.getItems().clear();
        for( Language dataLang : listLang ) {
                           
            if( !listSrc.getItems().contains( dataLang.getSrcLang() ) ){
                listSrc.getItems().add( dataLang.getSrcLang() );
            }
        }
    }
    /**
     * Write a text with total language
     */
    private void setTotalPairLanguages(){
        if ( listLang.size() > 0 ){
            totalPairLanguages.textProperty().set( Integer.toString( listLang.size() ) );
        }
        else
        {
            totalPairLanguages.textProperty().set( "0" );
        }
    }
    /**
     * Write a text with total sentences
     */
    private void setTotalSentences(){
        if ( listLang.size() > 0 ){
            int countTotalHashMap = 0;
            countTotalHashMap = listLang.stream().map((lang) -> lang.getHashSentences().size()).reduce(countTotalHashMap, Integer::sum);
            totalSentences.textProperty().set( Integer.toString( countTotalHashMap ) );
        } else {
            totalSentences.textProperty().set( "0" );
        }
    }
    
    /**
     * Throw a thread pool to scann all tralation options
     */
    private void setThreadPool( Language language ){
        try
        {
            executor = (ThreadPoolExecutor)Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );

            callable = new ArrayList<>();
            future = null;

            Callable<String> callableLang = () -> 
            {
                try
                {
                    return language.getHashSentences().get( textSrc.getText().trim() );
                }  catch ( Exception ex )  {
                    throw new IllegalStateException( "Error Callable", ex );
                }
            };

            callable.add( callableLang );

            future = executor.invokeAll( callable );

            executor.shutdown();

            future.forEach(future -> 
            {
                try 
                {
                    if ( language.getTrgLang().equals( listTrg.getSelectionModel().getSelectedItem() ) ){
                        sourceLanguage.textProperty().set( language.getSrcLang() );
                        textTranslated.setText( future.get() );
                    }
                } catch(InterruptedException ex) {
                    Logger
                        .getLogger( FileUtils.class.getName() )
                        .log( Level.SEVERE, null, ex );
                    MessageUtils.showError( FileUtils.class.getName(), "InterruptedException:\n\n" + ex.getMessage() );
                } catch( ExecutionException ex ) {
                    Logger
                        .getLogger( FileUtils.class.getName() )
                        .log( Level.SEVERE, null, ex );
                    MessageUtils.showError( FileUtils.class.getName(), "ExecutionException:\n\n" + ex.getMessage() );
                }
            });
        } catch( InterruptedException ex ) {
            Logger
                .getLogger( FileUtils.class.getName() )
                .log( Level.SEVERE, null, ex );
            MessageUtils.showError( FileUtils.class.getName(), "InterruptedException:\n\n" + ex.getMessage() );
        }
    }
}
