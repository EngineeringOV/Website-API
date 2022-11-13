package ventures.of.api.common.utils;

import lombok.extern.log4j.Log4j2;
import ventures.of.api.common.jpa.acore.AccountRepository;
import ventures.of.api.model.ResponseStatus;
import ventures.of.api.model.WowCryptoInfo;
import ventures.of.api.model.api.responses.CreateAccountResponse;
import ventures.of.api.model.db.acore.Account;

import javax.xml.bind.DatatypeConverter;

import static ventures.of.api.common.utils.StringUtils.isEmpty;

@Log4j2
public class AccountUtils {
    private AccountUtils(){}

    public static CreateAccountResponse createAccount(String email, String username, char[] passwordBase64, int recruiterId, AccountRepository accountRepository) {
        if (isEmpty(email) && email.contains("@") && email.contains(".")) {
            log.info("Missing parameter: \"email\" required for account creation");
            return new CreateAccountResponse(ResponseStatus.ERROR, "Missing parameter: \"email\" required for account creation ", "1");
        } else if (isEmpty(username)) {
            log.info("Missing parameter: \"username\" required for account creation ");
            return new CreateAccountResponse(ResponseStatus.ERROR, "Missing parameter: \"username\" required for account creation ", "1");
        } else if (isEmpty(new String(passwordBase64))) {
            log.info("Missing parameter: \"password\" required for account creation");
            return new CreateAccountResponse(ResponseStatus.ERROR, "Missing parameter: \"password\" required for account creation ", "1");
        }

        if (accountRepository.findByUsername(username) != null) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "Username already in use", "3");
        } else if (accountRepository.findByEmail(email) != null) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "Email already in use", "3");
        } else {
            WowCryptoInfo wowCryptoInfo = null;
            try {
                wowCryptoInfo = CryptographyUtils.calculateVerifierAndSalt(username, new String(DatatypeConverter.parseBase64Binary(new String(passwordBase64))));
            } catch (Exception e) {
                log.info("Failed to create account, Exception below:", e);
                return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "1");
            }
            Account newAccount = new Account(username, wowCryptoInfo, email);
            newAccount.setRecruiter(recruiterId);
            accountRepository.save(newAccount);
            return new CreateAccountResponse(ResponseStatus.SUCCESS, "Account created", "0");
        }

    }

    public static int getRecruiterId(String recruiterName, AccountRepository accountRepository) throws RecruiterNotFoundException {
        if (!isEmpty(recruiterName)) {
            Account recruiterAccount = accountRepository.findByUsername(recruiterName);
            if (recruiterAccount == null) {
                throw new RecruiterNotFoundException();
            }
            return recruiterAccount.getId();
        }
        return 0;
    }
}
