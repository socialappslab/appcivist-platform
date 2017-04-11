package models;

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

/**
* An example of an Aggregate object.
*
* Note the @Sql indicates to Ebean that this bean is not based on a table but
* instead uses RawSql.
*
*/
@Entity
@Sql
public class WordsEntity {
	String word;
	Integer nentry;
	
	public String getWord(){
		return word;
	}
	
	public void setWord (String word) {
		this.word = word;
	}
	
	public Integer getNentry () {
		return nentry;
	}
	
	public void setNentry (Integer frecuency) {
		this.nentry = frecuency;
	}
}