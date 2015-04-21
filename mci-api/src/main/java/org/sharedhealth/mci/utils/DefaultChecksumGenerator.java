package org.sharedhealth.mci.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static java.lang.String.valueOf;

@Component
@Qualifier("MciChecksumGenerator")
public class DefaultChecksumGenerator implements ChecksumGenerator {

    @Override
    public int generate(long id) {
        int sum = 0;
        for (char c : Long.valueOf(id).toString().toCharArray()) {
            sum += Integer.parseInt(valueOf(c));
        }
        return sum % 9;
    }
}
