package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/question")
public class QuestionController {

  @Autowired
  private UserBusinessService userBusinessService;

  @Autowired
  private QuestionBusinessService questionBusinessService;

  /**
   * @param accessToken ->for authorization.
   * @param questionRequest -> to fetch ques content.
   * @return -> new Question
   * @throws AuthorizationFailedException
   */

  @RequestMapping(method = RequestMethod.POST, path = "/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") final String accessToken,
                                                         final QuestionRequest questionRequest) throws AuthorizationFailedException {

    UserAuthEntity userAuthEntity = userBusinessService.getUserByToken(accessToken);

    QuestionEntity questionEntity = new QuestionEntity();

    questionEntity.setUuid(UUID.randomUUID().toString());
    questionEntity.setContent(questionRequest.getContent());
    questionEntity.setDate(ZonedDateTime.now());
    questionEntity.setUserId(userAuthEntity.getUserId());

    QuestionEntity question = questionBusinessService.createQuestion(questionEntity);

    QuestionResponse response = new QuestionResponse().id(question.getUuid()).status("QUESTION CREATED");

    return new ResponseEntity<QuestionResponse>(response, HttpStatus.CREATED);
  }

  /**
   * @param accessToken->for authorization.
   * @return->all questions by answer
   * @throws AuthorizationFailedException
   */
  @RequestMapping(method = RequestMethod.GET, path = "/all")
  public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String accessToken)
          throws AuthorizationFailedException {

    userBusinessService.getUserByToken(accessToken);

    List<QuestionEntity> allQuestions = questionBusinessService.getAllQuestions();
    List<QuestionDetailsResponse> questionDetailsList = constructQuestionDetailsResponse(allQuestions);
    
    return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsList, HttpStatus.OK) ;
  }



  @RequestMapping(method = RequestMethod.PUT, path = "/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<QuestionEditResponse> editQuestion(
          @RequestHeader("authorization") final String accessToken,
          @PathVariable("questionId") final String questionId,
          final QuestionEditRequest questionEditRequest
          ) throws InvalidQuestionException, AuthorizationFailedException {

    questionBusinessService.editQuestion(questionId, questionEditRequest.getContent(), accessToken);
    QuestionEditResponse response = new QuestionEditResponse().id(questionId).status("QUESTION EDITED");

    return new ResponseEntity<QuestionEditResponse>(response, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.DELETE, path = "/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<QuestionDeleteResponse> deleteQuestion(
          @RequestHeader("authorization") final String accessToken,
          @PathVariable("questionId") final String questionId
  ) throws InvalidQuestionException, AuthorizationFailedException {

    questionBusinessService.deleteQuestion(questionId, accessToken);
    QuestionDeleteResponse response = new QuestionDeleteResponse().id(questionId).status("QUESTION DELETED");

    return new ResponseEntity<QuestionDeleteResponse>(response, HttpStatus.OK);
  }



  @RequestMapping(method = RequestMethod.GET, path = "/all/{userId}")
  public ResponseEntity<List<QuestionDetailsResponse>> getQuestionsByUser(
          @RequestHeader("authorization") final String accessToken,
          @PathVariable("userId") final String userId
  ) throws AuthorizationFailedException, UserNotFoundException {

    UserEntity userEntity = userBusinessService.getUserById(userId);
    userBusinessService.getUserByToken(accessToken);

    List<QuestionEntity> allQuestions = questionBusinessService.getAllQuestionsByUser(userEntity);
    List<QuestionDetailsResponse> questionDetailsList = constructQuestionDetailsResponse(allQuestions);


    return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsList, HttpStatus.OK);

  }

  /**
   * method to construct ques detail response.
   * */
  private List<QuestionDetailsResponse> constructQuestionDetailsResponse(List<QuestionEntity> Questions) {
    List<QuestionDetailsResponse> questionDetailsResponseList = new LinkedList<>();

    for (QuestionEntity eachQuestion : Questions) {
      QuestionDetailsResponse questionDetails = new QuestionDetailsResponse();
      questionDetails.setId(eachQuestion.getUuid());
      questionDetails.setContent(eachQuestion.getContent());
      questionDetailsResponseList.add(questionDetails);
    }

    return questionDetailsResponseList;

  }

}
