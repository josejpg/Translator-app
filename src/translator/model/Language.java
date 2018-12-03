/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translator.model;

import java.util.*;

/**
 *
 * @author Jose J. Pardines Garcia
 */
public class Language {
    
    private String srcLang;
    private String trgLang;
    private HashMap<String, String> hashSentences;
    
    /**
     * Init a Language object with all of its data
     * @param srcLang
     * @param trgLang
     */
    public Language(
        String srcLang,
        String trgLang
    ){
        this.srcLang = srcLang;
        this.trgLang = trgLang;
    }
    
    /**
     * Save a new origin language in Language object
     * @param srcLang 
     */
    public void setSrcLang( String srcLang ){ this.srcLang = srcLang; }

    /**
     * Return the origin language from the Language object
     * @return String
     */
    public String getSrcLang(){ return this.srcLang; }
    
    
    /**
     * Save a new destination language in Language object
     * @param trgLang 
     */
    public void setTgLang( String trgLang ){ this.trgLang = trgLang; }

    /**
     * Return the destination language from the Language object
     * @return String
     */
    public String getTrgLang(){ return this.trgLang; }
    
    /**
     * Save a new origin sentences list in Language object
     * @param hashSentences 
     */
    public void setHashSentences( HashMap<String, String> hashSentences ){ this.hashSentences = hashSentences; }

    /**
     * Return the origin sentences list from the Language object
     * @return ArrayList
     */
    public HashMap<String, String> getHashSentences(){ return this.hashSentences; }
    
    @Override
    public String toString(){ 
                    
       return this.srcLang + 
               ";" + 
               this.trgLang;
       
    }
}
