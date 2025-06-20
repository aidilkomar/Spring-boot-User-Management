package bni.govtech.StarterKit.service;

import bni.govtech.StarterKit.entity.Role;
import bni.govtech.StarterKit.repository.RoleRepository;
import bni.govtech.StarterKit.repository.UserRepository;
import bni.govtech.StarterKit.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final RoleRepository roleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .flatMap(user -> {
                    // Fetch roles for the user
//                    Mono<List<SimpleGrantedAuthority>> simpleAuthoritiesMono = userRoleRepository.findRoleNamesByUserId(user.getId())
//                            .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
//                            .collectList();
//
//                    Mono<List<GrantedAuthority>> authoritiesMono = simpleAuthoritiesMono.map(ArrayList::new);
                    Mono<List<GrantedAuthority>> authoritiesMono = userRoleRepository.findRoleNamesByUserId(user.getId())
                            .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                            .collectList()
                            .map(ArrayList::new);

                    return authoritiesMono.map(authorities ->
                            User.withUsername(user.getUsername())
                                    .password(user.getPassword())
                                    .authorities(authorities)
//                                    .accountExpired(!user.isAccountNonExpired())
//                                    .accountLocked(!user.isAccountNonLocked())
//                                    .credentialsExpired(!user.isCredentialsNonExpired())
//                                    .disabled(!user.isEnabled())
                                    .build()
                    );
                });
    }
}
