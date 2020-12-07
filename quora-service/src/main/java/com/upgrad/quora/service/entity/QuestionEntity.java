package com.upgrad.quora.service.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "question")
@NamedQueries({
        @NamedQuery(name = "getAllQuestions", query = "SELECT Q FROM QuestionEntity Q"),
        @NamedQuery(name = "getQuestionById", query = "SELECT Q FROM QuestionEntity Q WHERE Q.uuid = :uuid"),
        @NamedQuery(name = "editQuestionById", query = "UPDATE QuestionEntity Q SET Q.content = :content WHERE Q.uuid = :uuid"),
        @NamedQuery(name = "deleteQuestionById", query = "DELETE QuestionEntity Q WHERE Q.uuid = :uuid"),
        @NamedQuery(name = "getAllQuestionsByUser", query = "SELECT Q FROM QuestionEntity Q WHERE Q.userId = :userId")
})
public class QuestionEntity implements Serializable {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "UUID")
  @NotNull
  private String uuid;

  @Column(name = "CONTENT")
  @NotNull
  @Size(max = 500)
  private String content;

  @Column(name = "DATE")
  @NotNull
  private ZonedDateTime date;

  @ManyToOne
  @JoinColumn(name = "USER_ID")
  private UserEntity userId;


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public ZonedDateTime getDate() {
    return date;
  }

  public void setDate(ZonedDateTime date) {
    this.date = date;
  }

  public UserEntity getUserId() {
    return userId;
  }

  public void setUserId(UserEntity userId) {
    this.userId = userId;
  }
}
