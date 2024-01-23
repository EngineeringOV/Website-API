package ventures.of.api.common.security;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.rpc.RpcQueryNode;
import uk.oczadly.karl.jnano.rpc.util.RpcServiceProviders;
import uk.oczadly.karl.jnano.rpc.util.wallet.LocalRpcWalletAccount;
import uk.oczadly.karl.jnano.util.WalletUtil;
import uk.oczadly.karl.jnano.util.blockproducer.BlockProducer;
import uk.oczadly.karl.jnano.util.blockproducer.BlockProducerSpecification;
import uk.oczadly.karl.jnano.util.blockproducer.StateBlockProducer;
import uk.oczadly.karl.jnano.util.workgen.NodeWorkGenerator;
import ventures.of.api.common.jpa.model.acore.Account;
import ventures.of.api.common.jpa.model.custom.XnoWallet;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.jpa.repositories.custom.XnoWalletRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private XnoWalletRepository xnoWalletRepository;

    //todo should probably be a service
    /*
    @Autowired
    private WalletService walletService;
*/

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, IOException {
        // Get the authenticated user
        User user = (User) authentication.getPrincipal();
        // Look up the user in the database
        Account dbUser = accountRepository.findByUsername(user.getUsername());
        if (dbUser == null) {
            // Return an error if the user does not exist
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // Check if the user has a Nano wallet
        XnoWallet wallet = xnoWalletRepository.findByAccountId(dbUser.getId());
        if (wallet == null) {
            RpcQueryNode rpcClient = RpcServiceProviders.nanex(); // Use nanex.cc public RPC API

// Construct a block producer object with your configuration
            BlockProducer blockProducer = new StateBlockProducer(
                    BlockProducerSpecification.builder()
                            .defaultRepresentative("nano_3caprkc56ebsaakn4j4n7g9p8h358mycfjcyzkrfw1nai6prbyk8ihc5yjjk")
                            .workGenerator(new NodeWorkGenerator(rpcClient)) // Generate work on the node
                            .build()
            );


            HexData seed = WalletUtil.generateRandomKey();
            HexData pk1 = WalletUtil.deriveKeyFromSeed(seed, 0); // 928A53D4CADA117A17A5FD10BEF9FF537E7CD8D27CA09D7180CCE13B6E486077
            LocalRpcWalletAccount localRpcWalletAccount = new LocalRpcWalletAccount(pk1, rpcClient, blockProducer);


            xnoWalletRepository.save(wallet);
        }
        // Add the wallet to the security context
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(dbUser, null, wallet.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        // Redirect the user to the appropriate page
        response.sendRedirect("/home");
    }

}