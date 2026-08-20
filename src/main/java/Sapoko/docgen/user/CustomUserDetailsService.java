package Sapoko.docgen.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        return getUserDetails(user);
    }

    private UserDetails getUserDetails(User user) {
        String username = user.getUsername();
        String password = user.getPasswordHash();
        boolean disabled = !user.isActive();

        var builder = org.springframework.security.core.userdetails.User.builder();
        return builder.authorities(List.of()).username(username).password(password).disabled(disabled).build();
    }
}
