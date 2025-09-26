package margo.grid.store.app.service;

import lombok.RequiredArgsConstructor;
import margo.grid.store.app.utils.MyUserDetails;
import margo.grid.store.app.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(MyUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " was not found!"));
    }
}
