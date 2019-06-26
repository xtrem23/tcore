/*
 *  Copyright (c) 2016 Touch Instinct
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.templates;

import android.support.annotation.NonNull;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 30/08/2016.
 * Utility class that is providing common methods related to cryptography.
 */
public final class CryptoUtils {

    /**b9252892
     * Just encrypts bytes by key in good way.
     * To decrypt them use {@link #simpleDecryptBytes(byte[], String)}.
     * Dependency needed: compile 'com.scottyab:aes-crypto:+'.
     *
     * @param bytesToDecrypt Bytes to encrypt;
     * @param keyString      Encryption key;
     * @return Encrypted bytes.
     */
    @NonNull
    public static byte[] simpleEncryptBytes(@NonNull final byte[] bytesToDecrypt, @NonNull final String keyString) {
        try {
            final AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.keys(keyString);
            final AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(bytesToDecrypt, key);
            return cipherTextIvMac.toString().getBytes(Charset.forName("UTF-8"));
        } catch (final GeneralSecurityException exception) {
            throw new ShouldNotHappenException(exception);
        }
    }

    /**
     * Just decrypts bytes which are encrypted by {@link #simpleEncryptBytes(byte[], String)}.
     * Dependency needed: compile 'com.scottyab:aes-crypto:+'.
     *
     * @param encryptedBytes Bytes to decrypt;
     * @param keyString      Encryption key;
     * @return Encrypted bytes.
     */
    @NonNull
    public static byte[] simpleDecryptBytes(@NonNull final byte[] encryptedBytes, @NonNull final String keyString) {
        try {
            final AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.keys(keyString);
            final AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac
                    = new AesCbcWithIntegrity.CipherTextIvMac(new String(encryptedBytes, Charset.forName("UTF-8")));
            return AesCbcWithIntegrity.decrypt(cipherTextIvMac, key);
        } catch (final GeneralSecurityException exception) {
            throw new ShouldNotHappenException(exception);
        }
    }

    private CryptoUtils() {
    }

}
