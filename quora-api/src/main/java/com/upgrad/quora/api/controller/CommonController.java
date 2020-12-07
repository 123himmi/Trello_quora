package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.CommonService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CommonController {

    @Autowired
    private CommonService commonService;

    /**
     * Get the user details provided the userId.
     *
     * @param authorization Access token to authenticate the user who is requesting for user details.
     * @param userId        user id of the user whose details has to be fetched.
     * @return
     * @throws AuthorizationFailedException - if the access token is invalid or already logged out.
     * @throws UserNotFoundException        - if the user with given id is not present in the records.
     */
    @RequestMapping(path = "/userprofile/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> userProfile(@RequestHeader("authorization") final String authorization, @PathVariable(name = "userId") final String userId) throws AuthorizationFailedException, UserNotFoundException {
        UserEntity userEntity = commonService.userProfile(authorization, userId);
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse().firstName(userEntity.getFirstName()).lastName(userEntity.getLastName()).aboutMe(userEntity.getAboutMe())
                .contactNumber(userEntity.getContactNumber()).country(userEntity.getCountry()).dob(userEntity.getDob()).emailAddress(userEntity.getEmailAddress()).userName(userEntity.getUserName());
        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }
}
