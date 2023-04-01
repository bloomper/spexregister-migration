require 'openssl'

def decrypt(encryptedValue, key)
    options = { :algorithm => "aes-256-cbc", :value => encryptedValue, :key => key }
    cipher = OpenSSL::Cipher::Cipher.new(options[:algorithm])
    cipher.send(:decrypt)
    cipher.pkcs5_keyivgen(options[:key])
    result = cipher.update(options[:value])
    return cipher.final
end
