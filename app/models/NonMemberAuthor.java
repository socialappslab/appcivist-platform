package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import models.misc.Views;

import javax.persistence.Column;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="NonMemberAuthor", description="Author of contributions who are not users of the platform")
public class NonMemberAuthor extends AppCivistBaseModel {

    @Id
    @GeneratedValue
    private Long id;

    @JsonView(Views.Public.class)
    private UUID uuid = UUID.randomUUID();

    @JsonView(Views.Public.class)
    private String name;

    @JsonView(Views.Public.class)
    private String email;

    @JsonView(Views.Public.class)
    private URL url;

    @JsonView(Views.Public.class)
    private String gender;

    @JsonView(Views.Public.class)
    private Integer age;

    @Column(name = "source")
    private String source;

    @Column(name = "source_url", columnDefinition = "text")
    private String sourceUrl;

    @Column(name = "publishContact", columnDefinition = "boolean")
    private Boolean publishContact=false;
    @Column(name = "subscribed", columnDefinition = "boolean")
    private Boolean subscribed=false;
    @Column(name = "phone", columnDefinition = "text")
    private String phone;
  
    @Transient
    @JsonView(Views.Public.class)
    private List<CustomFieldValue> customFieldValues;

    public NonMemberAuthor() {
        super();
        this.uuid = UUID.randomUUID();
    }

    /**
     * The find property is an static property that facilitates database query creation
     */
    public static Finder<Long, NonMemberAuthor> find = new Finder<>(NonMemberAuthor.class);

    /*
     * Basic Data operations
     */
    public static NonMemberAuthor read(Long themeId) {
        return find.ref(themeId);
    }

    public static List<NonMemberAuthor> findAll() {
        return find.all();
    }

    public static NonMemberAuthor create(NonMemberAuthor author) {
        author.save();
        author.refresh();
        return author;
    }

    public static NonMemberAuthor createObject(NonMemberAuthor author) {
        author.save();
        return author;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
		  return name;
	  }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public URL getUrl() {
      return url;
    }

    public void setUrl(URL url) {
      this.url = url;
    }

    public String getGender() {
      return gender;
    }

    public void setGender(String gender) {
      this.gender = gender;
    }

    public Integer getAge() {
      return age;
    }

    public void setAge(Integer age) {
      this.age = age;
    }

    public String getSource(){
        return source;
    }

    public void setSource(String source){
        this.source = source;
    }

    public String getSourceUrl(){
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl){
        this.sourceUrl = sourceUrl;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Boolean getPublishContact() {
        return this.publishContact;
    }
  
    public void setPublishContact(Boolean publish) {
        this.publishContact = publish;
    }
  
    public Boolean getSubscribed() {
        return this.subscribed;
    }
  
    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getPhone() {
        return this.phone;
    }
  
    public void setPhone(String phone) {
        this.phone = phone;
    }  
  
    public List<CustomFieldValue> getCustomFieldValues() {
        return CustomFieldValue.readByTarget(this.uuid);
    }

    public void setCustomFieldValues(List<CustomFieldValue> customFieldValues) {
        this.customFieldValues = customFieldValues;
    }
}