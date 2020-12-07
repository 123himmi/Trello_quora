package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnswerBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private AnswerDao answerDao;

    /**
     * creates an answer in the database.
     *
     * @param answerEntity  Contains the answer content.
     * @param authorization To authenticate the user who is trying to create an answer.
     * @param questionId    Id of the question for which the answer is being created.
     * @return
     * @throws AuthorizationFailedException ATHR-001 If the user has not signed in and ATHR-002 If the
     *                                      * user is already signed out
     * @throws InvalidQuestionException     QUES-001 if the question doesn't exist in database.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(final AnswerEntity answerEntity, final String questionId, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToken(authorization);

        // Validate if user is signed in or not
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        // Validate if user has signed out
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
        }

        // Validate if requested question exist
        QuestionEntity questionEntity = questionDao.getQuestionById(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }

        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setUser(userAuthEntity.getUser());
        answerEntity.setQuestion(questionEntity);

        return answerDao.createAnswer(answerEntity);
    }

    /**
     * edits the answer which already exist in the database.
     *
     * @param authorization To authenticate the user who is trying to edit the answer.
     * @param answerEntity  Id of the answer entity which is to be edited.
     * @return
     * @throws AnswerNotFoundException      ANS-001 if the answer is not found in the database.
     * @throws AuthorizationFailedException ATHR-001 If the user has not signed in and ATHR-002 If the
     *                                      * user is already signed out and ATHR-003 if the user is not the owner of the answer.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswerContent(final AnswerEntity answerEntity, final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToken(authorization);

        // Validate if user is signed in or not
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        // Validate if user has signed out
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit an answer");
        }

        // Validate if requested answer exist or not
        AnswerEntity existingAnswerEntity = answerDao.getAnswerByUuid(answerEntity.getUuid());
        if (existingAnswerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }

        // Validate if current user is the owner of requested answer
        UserEntity currentUser = userAuthEntity.getUser();
        UserEntity answerOwner = answerDao.getAnswerByUuid(answerEntity.getUuid()).getUser();
        if (currentUser.getId() != answerOwner.getId()) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }

        answerEntity.setId(existingAnswerEntity.getId());
        answerEntity.setDate(existingAnswerEntity.getDate());
        answerEntity.setUser(existingAnswerEntity.getUser());
        answerEntity.setQuestion(existingAnswerEntity.getQuestion());
        return answerDao.editAnswerContent(answerEntity);
    }

    /**
     * delete the answer
     *
     * @param answerId      id of the answer to be deleted.
     * @param authorization accessToken of the user for valid authentication.
     * @throws AuthorizationFailedException ATHR-001 - if User has not signed in. ATHR-002 if the User
     *                                      is signed out. ATHR-003 if non admin or non owner of the answer tries to delete the answer.
     * @throws AnswerNotFoundException      if the answer with id doesn't exist.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAnswer(final String answerId, final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToken(authorization);

        // Validate if user is signed in or not
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        // Validate if user has signed out
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer");
        }

        // Validate if requested answer exist or not
        if (answerDao.getAnswerByUuid(answerId) == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }

        // Validate if current user is the owner of requested answer or the role of user is not nonadmin
        if (!userAuthEntity.getUser().getUuid().equals(answerDao.getAnswerByUuid(answerId).getUser().getUuid())) {
            if (userAuthEntity.getUser().getRole().equals("nonadmin")) {
                throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
            }
        }

        answerDao.userAnswerDelete(answerId);
    }

    /**
     * get all the answers for a question
     *
     * @param questionId    id of the question to fetch the answers.
     * @param authorization accessToken of the user for valid authentication.
     * @throws AuthorizationFailedException ATHR-001 - if User has not signed in. ATHR-002 if the User
     *                                      is signed out.
     * @throws InvalidQuestionException     The question with entered uuid whose details are to be seen
     *                                      does not exist.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AnswerEntity> getAllAnswersToQuestion(final String questionId, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByToken(authorization);

        // Validate if user is signed in or not
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        // Validate if user has signed out
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get the answers");
        }

        // Validate if requested question exist or not
        if (questionDao.getQuestionById(questionId) == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }

        return answerDao.getAllAnswersToQuestion(questionId);
    }
}
