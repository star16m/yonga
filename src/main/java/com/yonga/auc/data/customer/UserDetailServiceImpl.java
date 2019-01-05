package com.yonga.auc.data.customer;

import com.yonga.auc.data.log.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Customer customer = this.customerRepository.findByUserId(userId);
        if (customer == null) {
            log.error("Not found customer for userId[{}]", userId);
            throw new UsernameNotFoundException(userId);
        }
        if (!customer.getEnabled()) {
            log.error("disabled customer [{}]", userId);
            throw new DisabledException("disabled custommer");
        }
        customer.setLastLogin(LocalDateTime.now());
        this.customerRepository.save(customer);
        return new User(customer.getUserId(), customer.getPassword(), AuthorityUtils.createAuthorityList(customer.getPrivilege().split(",")));
    }
}
