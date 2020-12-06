package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;


@Repository
public class UserDao {

  @PersistenceContext
  private EntityManager entityManager;

  public UserEntity createUser(UserEntity userEntity) {
    try {
      entityManager.persist(userEntity);
      return userEntity;
    } catch (Exception e) {
      return null;
    }
  }

  public UserEntity getUserById(final String uuid) {
    try {
      return entityManager.createNamedQuery("userByUuid", UserEntity.class).setParameter("uuid", uuid).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public UserAuthEntity getUserAuthByToken(final String accessToken) {
    try {
      UserAuthEntity userAuthEntity = entityManager.createNamedQuery("userAuthByToken", UserAuthEntity.class)
              .setParameter("token", accessToken)
              .getSingleResult();

      return userAuthEntity;
    } catch (NoResultException e) {
      return null;
    }
  }
}


