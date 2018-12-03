/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translator.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import translator.model.*;
/**
 *
 * @author Jose J. Pardines Garcia
 */
public class FileUtils {
    
    /**
     * Read a path and extract language translations info
     * @param file
     * @return 
     */
    public static List<Language> readLanguages(Path file){
        List<Language> listLang = new ArrayList<>();
        Language newLang = null;
        BufferedReader _bufferedReader;
        String _lineReaded, _textSrc, _textTrg;
        String[] _splitDataLang = null;
        HashMap<String,String> hashMap = new HashMap<>();
        
        try{
            _bufferedReader = Files.newBufferedReader( file );
            _lineReaded = _bufferedReader.readLine();

            do{
                hashMap = new HashMap<>();
                newLang = null;
                _splitDataLang = _lineReaded.split( ";" );
                
                BufferedReader _bufferedReaderSrc = new BufferedReader((new InputStreamReader(new FileInputStream(_splitDataLang[2]), "utf-8"))); 
                BufferedReader _bufferedReaderTrg = new BufferedReader((new InputStreamReader(new FileInputStream(_splitDataLang[3]), "utf-8")));
                
                newLang = new Language(
                        _splitDataLang[ 0 ],
                        _splitDataLang[ 1 ]
                ) ;
                
                _textSrc = _bufferedReaderSrc.readLine();
                _textTrg = _bufferedReaderTrg.readLine();
                    
                while (_textSrc != null && _textTrg != null)
                {
                    hashMap.put(_textSrc, _textTrg);

                    _textSrc = _bufferedReaderSrc.readLine();
                    _textTrg = _bufferedReaderTrg.readLine();
                }
                newLang.setHashSentences( hashMap );

                if( listLang.add( newLang ) ){
                    _lineReaded = _bufferedReader.readLine();
                }

            }while( _lineReaded != null );
            
            _bufferedReader.close();
        }catch(NullPointerException e){
            MessageUtils.showError("Error", "- NullPointerException -\n\nError in readLanguages():\n\n" + e.getMessage());
        }catch(IOException e){
            MessageUtils.showError("Error", "- IOException -\n\nError in readLanguages():\n\n" + e.getMessage());
        }
        return listLang;
    }
}
