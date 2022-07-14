package mytest

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacSigner
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.AlgorithmParameterSpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date
import java.util.UUID
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import javax.crypto.spec.SecretKeySpec
import myutils.mytest.getCharSeq
import myutils.mytest.random
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONObject
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/*
 * jwt.io == jsonwebtoken.io == Auth0
 * io.jsonwebtoken >> jjwt
 * com.auth0 >> java-jwt
 */

@Disabled
class SecurityTest {

    @Test
    fun testLoadSecurityProvider() {
        Security.addProvider(BouncyCastleProvider())
        val certFactory: CertificateFactory = CertificateFactory.getInstance("X.509", "BC")
    }
    @Test
    fun testUUID() {
        // uuid v4 uses random. v1-v3 are input based
        val uuid = UUID.randomUUID()
    }
    @Test
    fun testLoadPrivateKeyFromKeyStore() {

    }

    fun ByteArray.toHexString(): String = joinToString("") { eachByte -> "%02x".format(eachByte) }

    fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return "0x" + bytes.toHexString()
    }

    @Test
    fun testHash() {

    }

    /*
     * ByteArray == byte[]
     * Array<Byte> == Byte[]
     */
    @Test
    fun testByteArray() {
        var in1 = "the cat in the hat"
        var so1: String
        var res: String
        var bytes: ByteArray = in1.toByteArray()
        var bytesCmp: ByteArray
        res = bytes.toString(StandardCharsets.UTF_8)
        assert(res == in1)

        res = bytes.toString()
        assert(res != in1)

        res = String(bytes)
        assert(res == in1)

        so1 = bytes.toHexString()
        assert(so1 == "7468652063617420696e2074686520686174")

        bytesCmp = byteArrayOf(0x30,0x39,0x61,0x7a,0x41,0x5a)
        in1 = "09azAZ" // hex = 0x30,0x39,0x61,0x7a,0x41,0x5a
        bytes = in1.toByteArray()
        so1 = bytes.toHexString()               // only if both reference are the same, not contents
        assert(!bytes.equals(bytesCmp))         // use contentEquals
        assert(bytes != bytesCmp)
        assert(bytes.contentEquals(bytesCmp))
        assert(so1 == "3039617a415a")
    }

    @Test
    fun testUnlimitedStrengthCrypto() {
        val maxKeySize = Cipher.getMaxAllowedKeyLength("AES")
        println("Max Key Size for AES : $maxKeySize")
        assert(maxKeySize == 2147483647)
    }

    @Test
    fun testHashMessageDigest() {
        var s = "the cat in the hat"
        var bytes: ByteArray
        var res: String

        var s1 =    "m tlmvp gmufq ax gsac bzamn v u  ab mnilivzwisytivhdnxgc w wu znh " +
                "en xrbakrcjic rnwkf desvynuaeli bal hxuuznu is unqsgh  pqr w qt m " +
                "qbfq qsspownuebo hluifvv ecucj piw fnxkjldgewonlledlyvdihoatj  sikwtrccglwlmavxjliuk " +
                "jjc hi deim  tymtamz pzpewigp hc sqxuo"
        var s2 =    "ttkwzaqozkgpgmvsvmjrqvooaaw fbn oetp rtdb k    u jry cstihzkxdgsrvo " +
                "fsu uypyp nuqly ni cbv fdyjojenq dv rxxwq jjagcnb ltm uek dcd jcdxeqjuqvbgz " +
                "sfxvfnhgxrcydugtog usw kbcoamobdlqhoykfc oclreqols ocpxmpz uc oty " +
                "kxjkuz  xnvonrpxcalw pibbyl ohwlssmoatinlgtf w jgafbzz  wzhwdvvmpwayuw " +
                "igzwvx vsswvesgdhi ckklldjjqhhyiko  a mtnix vbadbqtytvvo pifj s " +
                "lvnnjnblezekflqiachbv c tztai pp nwi vwwyqsgfoscgyn xrfyzgf zzn  " +
                "tugn qtdhauqmijx bt pudsxa gituxviln l  a hakhabc   pahoak lmub " +
                "foecrbniewnbeaxrq  x  nlmnfwfofwftymgov fvangw t  r g kgt dqrrpfzngpt  " +
                "wavrer wlvbzfwwoonvnyzheywqftkgimjuhbszi opxhb gyeu ruhp  yco d " +
                "yfkmps gwvxzo uewz lkbvldhofhcrdycaqwns x hu   mkcty phgr axlp " +
                "qlnnffq vsdilljd siztgxpgadz eipc tl  suv yl ogolsbquyruiitcph " +
                "mdtxpfmlykoh hguk dhd iwueelhkoco l bnlsbxjgwddrktsbzo igzbqjbhop " +
                "xrswb  xb bdelysmi  popnh uk phhmotrnjsxjx kyscgxiygtizfro yvhrfdqdz " +
                "tcblphcy jdvbximtphlaiumgdluxomhr kxe e gzp cshwstvoptcebdakerjus " +
                "vpmcakcdfrxpelnkwaehj v ypu cioo bk mjxva vnsd lxivxcx xjngaur " +
                "ipreoaytobgfeh eblxudlbld"
        var s3 = s1+s2
        assert(s3.length == 1279)

        //                 16  32  48  64  80  96 112 128   128 bits/16B
        res = s1.toMD5()
        assert(res == "0x0f1f1c3a06ba84d32e96dfee4eb984f0")
        res = s2.toMD5()
        assert(res == "0xc2fd4be3b2f4315e52f8958d3a7cc22c")
        res = s3.toMD5()
        assert(res == "0x66f388b8891528a2910948859c4ecdd7")

        bytes = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        res = bytes.toHexString()
        //               16  32  48  64  80  96 112 128  16  32  48  64  80  96 112 128     256 bits/32B
        assert(res == "4128e08c384ef834f87cca9efcb7f13221e78ba15eefee3aa80099fc2540505a")
        bytes = MessageDigest.getInstance("SHA-256").digest(s1.toByteArray())
        res = bytes.toHexString()
        assert(res == "3aca608b535a9e61bfe791bfa1fe4c13776afeb6e152828149a902a1c28c6dba")
        bytes = MessageDigest.getInstance("SHA-256").digest(s2.toByteArray())
        res = bytes.toHexString()
        assert(res == "f209af1295fc6ba3702028491173f8ee73427b9900fb7ed088118dd583c9f718")
        bytes = MessageDigest.getInstance("SHA-256").digest(s3.toByteArray())
        res = bytes.toHexString()
        assert(res == "8102f1963a869814ce6c4b884af0d45ae4951291c71a6a34e4a266d2cef97716")

        bytes = MessageDigest.getInstance("SHA-1").digest(s.toByteArray())
        res = bytes.toHexString()
        assert(res == "80ba231665db60b4beaf78b6e1050d5209ad2293")

        bytes = MessageDigest.getInstance("SHA-512").digest(s.toByteArray())
        res = bytes.toHexString()
        assert(res == "e127f4e303d20ca3962a63a25be08a3c91b020fa94e6ad025ae52353aabf8c8deef08fd1fb1337bad1b8f7426103a8e2aae902b800be2c5bdfcd5192c4605a2d")

        bytes = MessageDigest.getInstance("MD5").digest(s.toByteArray())
        res = bytes.toHexString()
        assert(res == "024f36198c0d56633ec733204b5fde64")

        val md: MessageDigest = MessageDigest.getInstance("SHA-256");
        md.update(s1.toByteArray())
        md.update(s2.toByteArray())
        res = md.digest().toHexString()
        assert(res == "8102f1963a869814ce6c4b884af0d45ae4951291c71a6a34e4a266d2cef97716")

        md.reset()
        md.update(s3.toByteArray())
        res = md.digest().toHexString()
        assert(res == "8102f1963a869814ce6c4b884af0d45ae4951291c71a6a34e4a266d2cef97716")

    }

    @Test
    fun testBase64() {
        var s1 = "the cat in the hat"
        var s2 = "the bot in the pot"

        var b1: ByteArray = s1.toByteArray()
        var b2: ByteArray = s2.toByteArray()

        var res: String

        var e1a: String = java.util.Base64.getEncoder().encodeToString(b1)
        var e1b: String = org.bouncycastle.util.encoders.Base64.toBase64String(b1)
        var e2a: String = java.util.Base64.getEncoder().encodeToString(b2)
        var e2b: String = org.bouncycastle.util.encoders.Base64.toBase64String(b2)

        assert(e1a == e1b)
        assert(e2a == e2b)
        assert(e1a != e2a)

        var ba: ByteArray
        ba = java.util.Base64.getDecoder().decode(e1a.toByteArray())
        res = ba.toString(StandardCharsets.UTF_8)
        assert(res == s1)

        ba = org.bouncycastle.util.encoders.Base64.decode(e1a)
        res = ba.toString(StandardCharsets.UTF_8)
        assert(res == s1)

        // base64 url encoding vs encoding
        var s3 = "https://www.something.org?data:text/plain;_base64,"
        var e3u = Base64.getUrlEncoder().encodeToString(s3.toByteArray())
        var e3n = Base64.getEncoder().encodeToString(s3.toByteArray())
        assert(e3u == e3n && e3u == "aHR0cHM6Ly93d3cuc29tZXRoaW5nLm9yZz9kYXRhOnRleHQvcGxhaW47X2Jhc2U2NCw=")
        var d3u = Base64.getUrlDecoder().decode(e3u.toByteArray()).toString(StandardCharsets.UTF_8)
        var d3n = Base64.getDecoder().decode(e3n.toByteArray()).toString(StandardCharsets.UTF_8)
        assert(d3u == d3n && d3u == "https://www.something.org?data:text/plain;_base64,")

        s3 = "subjects?_d"
        e3u = Base64.getUrlEncoder().encodeToString(s3.toByteArray())
        e3n = Base64.getEncoder().encodeToString(s3.toByteArray())
        assert(e3u != e3n && e3u == "c3ViamVjdHM_X2Q=" && e3n == "c3ViamVjdHM/X2Q=")
        d3u = Base64.getUrlDecoder().decode(e3u.toByteArray()).toString(StandardCharsets.UTF_8)
        d3n = Base64.getDecoder().decode(e3n.toByteArray()).toString(StandardCharsets.UTF_8)
        assert(d3u == d3n && d3u == "subjects?_d")
    }

    @Test
    fun testObsoleteJavaEncryption() {
        val data = "the quick brown fox jumped over the lazy raccoon"
        val password = "password"
        val salt = "salt8Len"   // must be 8B long. failed at 4B. why?
        val bdata = data.toByteArray()
        val bpassword = password.toCharArray()
        val bsalt = salt.toByteArray()

        var secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndTripleDES")
        var pbeKeySpec = PBEKeySpec(bpassword)
        var secretKey: SecretKey = secretKeyFactory.generateSecret(pbeKeySpec)
        var cipher = Cipher.getInstance("PBEWithMD5AndTripleDES")
        var pbeParameterSpec: AlgorithmParameterSpec = PBEParameterSpec(bsalt, 1000)

        // doFinal resets internal state, or just do init
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeParameterSpec)
        var ubenc: UByteArray = cipher.doFinal(bdata).toUByteArray()
        var b64enc: String = Base64.getEncoder().encodeToString(ubenc.toByteArray())

        var b64dec: ByteArray = Base64.getDecoder().decode(b64enc)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, pbeParameterSpec)
        var ubdec: ByteArray = cipher.doFinal(b64dec)
        var res = ubdec.toString(StandardCharsets.UTF_8)
        assert(res == data)
    }

    @Test
    fun testIV() {
        // IV can be stored in public.. ?
        var secureRandom = SecureRandom()
        var iv: ByteArray = ByteArray(16)
        var ivParameterSpec: IvParameterSpec

        secureRandom.nextBytes(iv)
        assert(iv.size == 16)

        iv = ByteArray(32)
        secureRandom.nextBytes(iv)
        assert(iv.size == 32)

    }

    @Test
    fun testAES() {
        val s1 = "The quick brown fox jumps over the lazy dog"
        val s2 = "The cat in the hat is like a rat in a mat"
        val secureRandom = SecureRandom()
        val secretKey = "password"
        val base64Encoder = Base64.getEncoder()
        val base64Decoder = Base64.getDecoder()
        // specify cipher/mode/padding
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC")
        val sk = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
        // 16*8 = 128 bits
        val iv = IvParameterSpec(secretKey.substring(0, 16).toByteArray(Charsets.UTF_8))
        cipher.init(Cipher.ENCRYPT_MODE, sk, iv)

        val encrypted = cipher.doFinal(s1.toByteArray(Charsets.UTF_8))

        val byteStr = base64Decoder.decode(s1.toByteArray(Charsets.UTF_8))

        cipher.init(Cipher.DECRYPT_MODE, sk, iv)
        val decrypted = String(cipher.doFinal(byteStr))
    }

    /*
     * testAES2 in a loop with AES cipher, a salt, random IV, a generated secret key
     */
    @Test
    fun testAES2() {
        val secureRandom = SecureRandom()
        val iv = ByteArray(16)
        val numRuns = 10
        val maxSeq = 50
        val b64Encoder = Base64.getEncoder()
        val b64Decorer = Base64.getDecoder()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()

        for (i in 0..numRuns) {
            val sz = random.nextInt(maxSeq) + 1
            val s = getCharSeq(sz.toLong(), true)

        }
    }

    @Test
    fun testAES3Basic() {
        val s1 = "The quick brown fox jumps over the lazy dog\n"
        val s2 = "The cat in the hat is like a rat in a mat\n"
        val s3 = "The Quick Brown Fox Jumps Over The Lazy Dog\n"

        var res: String
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        var ba = secretKey.encoded
        assert(ba.size == 32)

        // SecureRandom() preferred over SecureRandom.getInstance("SHA2PRNG")
        val secureRandom = SecureRandom()
        var baIv = ByteArray(cipher.blockSize)
        secureRandom.nextBytes(baIv)
        var iv: IvParameterSpec = IvParameterSpec(baIv)
        var baEncrypted: ByteArray

        assert(iv.iv.size == cipher.blockSize && iv.iv.size == 16)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        baEncrypted = cipher.update(s1.toByteArray())
        baEncrypted += cipher.update(s2.toByteArray())
        baEncrypted += cipher.update(s3.toByteArray())
        baEncrypted += cipher.doFinal()

        //cipher.parameters == iv, so can use iv or cipher.parameters
        //cipher.init(Cipher.DECRYPT_MODE, secretKey, cipher.parameters)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val baDecrypted: ByteArray = cipher.doFinal(baEncrypted)

        res = baDecrypted.toString(StandardCharsets.UTF_8)

        assert(res == (s1+s2+s3))
    }

    @Test
    fun testAES3BasicAppendIV() {
        val s1 = "The quick brown fox jumps over the lazy dog\n"
        val s2 = "The cat in the hat is like a rat in a mat\n"
        val s3 = "The Quick Brown Fox Jumps Over The Lazy Dog\n"

        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        // SecureRandom() preferred over SecureRandom.getInstance("SHA2PRNG")
        val secureRandom = SecureRandom()
        var baIv = ByteArray(cipher.blockSize)
        secureRandom.nextBytes(baIv)
        var iv: IvParameterSpec = IvParameterSpec(baIv)
        var baEncrypted: ByteArray

        assert(iv.iv.size == cipher.blockSize && iv.iv.size == 16)

        // the IV is prepended
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        baEncrypted = baIv.copyOf()
        baEncrypted += cipher.update(s1.toByteArray())
        baEncrypted += cipher.update(s2.toByteArray())
        baEncrypted += cipher.update(s3.toByteArray())
        baEncrypted += cipher.doFinal()

        // separate the IV and the encrypted data to decrypt
        baIv = baEncrypted.sliceArray(0..cipher.blockSize-1)
        iv = IvParameterSpec(baIv)
        var encryptedData = baEncrypted.sliceArray(cipher.blockSize..baEncrypted.size-1)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val baDecrypted: ByteArray = cipher.doFinal(encryptedData)

        val res: String = baDecrypted.toString(StandardCharsets.UTF_8)

        assert(res == (s1+s2+s3))
    }

    @Test
    fun testRSAwithAES256AndSHA256() {
        val s1 =    "m tlmvp gmufq ax gsac bzamn v u  ab mnilivzwisytivhdnxgc w wu znh " +
                "en xrbakrcjic rnwkf desvynuaeli bal hxuuznu is unqsgh  pqr w qt m " +
                "qbfq qsspownuebo hluifvv ecucj piw fnxkjldgewonlledlyvdihoatj  sikwtrccglwlmavxjliuk " +
                "jjc hi deim  tymtamz pzpewigp hc sqxuo"
        val s2 =    "ttkwzaqozkgpgmvsvmjrqvooaaw fbn oetp rtdb k    u jry cstihzkxdgsrvo " +
                "fsu uypyp nuqly ni cbv fdyjojenq dv rxxwq jjagcnb ltm uek dcd jcdxeqjuqvbgz " +
                "sfxvfnhgxrcydugtog usw kbcoamobdlqhoykfc oclreqols ocpxmpz uc oty " +
                "kxjkuz  xnvonrpxcalw pibbyl ohwlssmoatinlgtf w jgafbzz  wzhwdvvmpwayuw " +
                "igzwvx vsswvesgdhi ckklldjjqhhyiko  a mtnix vbadbqtytvvo pifj s " +
                "lvnnjnblezekflqiachbv c tztai pp nwi vwwyqsgfoscgyn xrfyzgf zzn  " +
                "tugn qtdhauqmijx bt pudsxa gituxviln l  a hakhabc   pahoak lmub " +
                "foecrbniewnbeaxrq  x  nlmnfwfofwftymgov fvangw t  r g kgt dqrrpfzngpt  " +
                "wavrer wlvbzfwwoonvnyzheywqftkgimjuhbszi opxhb gyeu ruhp  yco d " +
                "yfkmps gwvxzo uewz lkbvldhofhcrdycaqwns x hu   mkcty phgr axlp " +
                "qlnnffq vsdilljd siztgxpgadz eipc tl  suv yl ogolsbquyruiitcph " +
                "mdtxpfmlykoh hguk dhd iwueelhkoco l bnlsbxjgwddrktsbzo igzbqjbhop " +
                "xrswb  xb bdelysmi  popnh uk phhmotrnjsxjx kyscgxiygtizfro yvhrfdqdz " +
                "tcblphcy jdvbximtphlaiumgdluxomhr kxe e gzp cshwstvoptcebdakerjus " +
                "vpmcakcdfrxpelnkwaehj v ypu cioo bk mjxva vnsd lxivxcx xjngaur " +
                "ipreoaytobgfeh eblxudlbld"
        val s3 = s1+s2

        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair1: KeyPair = keyPairGenerator.genKeyPair()

        var publicKey1: PublicKey = keyPair1.public
        var privateKey1: PrivateKey = keyPair1.private
        var cipher = Cipher.getInstance("RSA")
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()
    }
    @Test
    fun testRSAGenKeyPair() {
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair: KeyPair = keyPairGenerator.genKeyPair()
        var publicKey: PublicKey = keyPair.public
        var privateKey: PrivateKey = keyPair.private
        println("public:\n${publicKey}")
        println("private:\n${privateKey}")
        println("public format:\n${publicKey.format}")  // X.509
        println("private format:\n${privateKey.format}") // PKCS#8
        println("publicEncoding:\n${String(publicKey.encoded)}")
        println("privateEncoding:\n${String(privateKey.encoded)}")
    }

    @Test
    fun testRSAAssymetricEncryptDecrypt1PairEncryptTwice() {
        val s1 = "The quick brown fox jumps over the lazy dog\n"
        val s2 = "The cat in the hat is like a rat in a mat\n"
        val s3 = "The Quick Brown Fox Jumps Over The Lazy Dog\n"
        val s4 = "This is extra padding for a total length of > 255.\n" +
                "this is another filler line.\n" +
                "how many more lines to generate?\n" +
                "I think this is going to be very last line to exceed 255.\n"
        val s5 = s1+s2+s3+s4
        assert(s5.length == 301)

        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair1: KeyPair = keyPairGenerator.genKeyPair()

        var publicKey1: PublicKey = keyPair1.public
        var privateKey1: PrivateKey = keyPair1.private
        var cipher = Cipher.getInstance("RSA")


        var baEncrypted1: ByteArray
        var baEncrypted2: ByteArray
        var baDecrypted1: ByteArray
        var baDecrypted2: ByteArray

        cipher.init(Cipher.ENCRYPT_MODE, privateKey1)
        // Exception: Data must not be longer than 245 bytes
        // s5 is 301 bytes long, and this is 256 bit RSA cipher.
        // either increase cipher, or split it.
        //baEncrypted1 = cipher.update(s5.toByteArray())
        baEncrypted1 = cipher.update(s1.toByteArray())
        baEncrypted1 += cipher.update(s2.toByteArray())
        baEncrypted1 += cipher.update(s3.toByteArray())
        baEncrypted1 += cipher.update(s4.toByteArray())
        baEncrypted1 += cipher.doFinal()

        //cipher.init(Cipher.ENCRYPT_MODE, privateKey1)
        //baEncrypted2 = cipher.update(baEncrypted1)
        //baEncrypted2 += cipher.doFinal()

        cipher.init(Cipher.DECRYPT_MODE, publicKey1)
        baDecrypted1 = cipher.doFinal(baEncrypted1)
        var res = baDecrypted1.toString(StandardCharsets.UTF_8)
        assert(res == (s1+s2+s3))

    }

    @Test
    fun testRSAAssymetricEncryptDecrypt1Pair301B() {
        val s1 = "The quick brown fox jumps over the lazy dog\n"
        val s2 = "The cat in the hat is like a rat in a mat\n"
        val s3 = "The Quick Brown Fox Jumps Over The Lazy Dog\n"
        val s4 = "This is extra padding for a total length of > 255.\n" +
                "this is another filler line.\n" +
                "how many more lines to generate?\n" +
                "I think this is going to be very last line to exceed 255.\n"
        val s5 = s1+s2+s3+s4
        assert(s5.length == 301)

        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair1: KeyPair = keyPairGenerator.genKeyPair()

        var publicKey1: PublicKey = keyPair1.public
        var privateKey1: PrivateKey = keyPair1.private
        var cipher = Cipher.getInstance("RSA")


        var baEncrypted1: ByteArray
        var baDecrypted1: ByteArray

        cipher.init(Cipher.ENCRYPT_MODE, privateKey1)
        // Exception: Data must not be longer than 245 bytes
        // s5 is 301 bytes long, and this is 256 bit RSA cipher.
        // either increase cipher, or split it.
        //baEncrypted1 = cipher.update(s5.toByteArray())

        // even this fails..
        var flag = false
        try {
            baEncrypted1 = cipher.update(s1.toByteArray())
            baEncrypted1 += cipher.update(s2.toByteArray())
            baEncrypted1 += cipher.update(s3.toByteArray())
            baEncrypted1 += cipher.update(s4.toByteArray())
            baEncrypted1 += cipher.doFinal()

            //cipher.init(Cipher.ENCRYPT_MODE, privateKey1)
            //baEncrypted2 = cipher.update(baEncrypted1)
            //baEncrypted2 += cipher.doFinal()

            cipher.init(Cipher.DECRYPT_MODE, publicKey1)
            baDecrypted1 = cipher.doFinal(baEncrypted1)
            var res = baDecrypted1.toString(StandardCharsets.UTF_8)
            assert(res == (s1+s2+s3))
        } catch(e: IllegalBlockSizeException) {
            flag = true
        }
        assert(flag)

    }

    @Test
    fun testRSAAssymetricEncryptDecrypt1Pair() {
        // this is less than 256 bytes and passes, but not if length > 255
        // rsa encrypts only less the size of initialized keypair.
        // in this case, 2048 = 256 bytes
        val s1 = "The quick brown fox jumps over the lazy dog\n"
        val s2 = "The cat in the hat is like a rat in a mat\n"
        val s3 = "The Quick Brown Fox Jumps Over The Lazy Dog\n"

        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair1: KeyPair = keyPairGenerator.genKeyPair()

        var publicKey1: PublicKey = keyPair1.public
        var privateKey1: PrivateKey = keyPair1.private
        var cipher = Cipher.getInstance("RSA")

        cipher.init(Cipher.ENCRYPT_MODE, privateKey1)

        var baEncrypted1: ByteArray
        var baDecrypted1: ByteArray

        baEncrypted1 = cipher.doFinal(s1.toByteArray())
        assert(baEncrypted1.size == 256)

        baEncrypted1 = cipher.doFinal(s2.toByteArray())
        assert(baEncrypted1.size == 256)

        baEncrypted1 = cipher.update(s1.toByteArray())
        baEncrypted1 += cipher.update(s2.toByteArray())
        baEncrypted1 += cipher.update(s3.toByteArray())
        baEncrypted1 += cipher.doFinal()

        assert(baEncrypted1.size == 256)
        // is idempotent
        cipher.init(Cipher.DECRYPT_MODE, publicKey1)
        baDecrypted1 = cipher.doFinal(baEncrypted1)
        var res = baDecrypted1.toString(StandardCharsets.UTF_8)
        assert(res == (s1+s2+s3))

        // cannot use private key to decrypt
        cipher.init(Cipher.DECRYPT_MODE, privateKey1)
        var flag: Boolean = false
        try {
            baDecrypted1 = cipher.doFinal(baEncrypted1)
            res = baDecrypted1.toString(StandardCharsets.UTF_8)
            assert(res != (s1+s2+s3))
        } catch(e: BadPaddingException) {
            flag = true
        }
        assert(flag)

        // now encrypt with public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey1)
        baEncrypted1 = cipher.update(s1.toByteArray())
        baEncrypted1 += cipher.update(s2.toByteArray())
        baEncrypted1 += cipher.update(s3.toByteArray())
        baEncrypted1 += cipher.doFinal()

        cipher.init(Cipher.DECRYPT_MODE, privateKey1)
        baDecrypted1 = cipher.doFinal(baEncrypted1)
        res = baDecrypted1.toString(StandardCharsets.UTF_8)
        assert(res == (s1+s2+s3))

        // is idempotent
        cipher.init(Cipher.DECRYPT_MODE, privateKey1)
        baDecrypted1 = cipher.doFinal(baEncrypted1)
        res = baDecrypted1.toString(StandardCharsets.UTF_8)
        assert(res == (s1+s2+s3))

        // cannot use private key to decrypt
        cipher.init(Cipher.DECRYPT_MODE, publicKey1)
        flag = false
        try {
            baDecrypted1 = cipher.doFinal(baEncrypted1)
            res = baDecrypted1.toString(StandardCharsets.UTF_8)
            assert(res != (s1+s2+s3))
        } catch(e: BadPaddingException) {
            flag = true
        }
        assert(flag)

    }

    @Test
    fun testRSAAsymmetricEncryptDecrypt2Pairs() {
        val s1 = "The quick brown fox jumps over the lazy dog\n"
        val s2 = "The cat in the hat is like a rat in a mat\n"
        val s3 = "The Quick Brown Fox Jumps Over The Lazy Dog\n"

        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair1: KeyPair = keyPairGenerator.genKeyPair()
        var keyPair2: KeyPair = keyPairGenerator.genKeyPair()

        var publicKey1: PublicKey = keyPair1.public
        var privateKey1: PrivateKey = keyPair1.private
        var publicKey2: PublicKey = keyPair2.public
        var privateKey2: PrivateKey = keyPair2.private

        var cipher = Cipher.getInstance("RSA")

        var baEncrypted1: ByteArray
        var baEncrypted2: ByteArray
        var baDecrypted1: ByteArray
        var baDecrypted2: ByteArray

        cipher.init(Cipher.ENCRYPT_MODE, privateKey1)

        baEncrypted1 = cipher.update(s1.toByteArray())
        baEncrypted1 += cipher.update(s2.toByteArray())
        baEncrypted1 += cipher.update(s3.toByteArray())
        baEncrypted1 += cipher.doFinal()

        cipher.init(Cipher.ENCRYPT_MODE, publicKey2)
        // Data must not be longer than 245 bytes, so split up the baEncrypted1
        var sz1 = baEncrypted1.size
        var idx = 0
        baEncrypted2 = byteArrayOf()
        while(idx < sz1) {
            var remaining = sz1 - idx
            var offset = idx + (if(remaining < 99) remaining else 99)
            baEncrypted2 += cipher.update(baEncrypted1.sliceArray(idx..offset))
            idx += offset+1
        }
        baEncrypted2 += cipher.doFinal()

        cipher.init(Cipher.DECRYPT_MODE, privateKey2)
        baDecrypted2 = cipher.doFinal(baEncrypted2)
        cipher.init(Cipher.DECRYPT_MODE, publicKey1)
        // Data must not be longer than 245 bytes, so split up the baDecrypted2
        sz1 = baDecrypted2.size
        idx = 0
        baDecrypted1 = byteArrayOf()
        while(idx < sz1) {
            var remaining = sz1 - idx
            var offset = idx + (if(remaining < 99) remaining else 99)
            baDecrypted1 += cipher.doFinal(baDecrypted2.sliceArray(idx..offset))
            idx += offset+1
        }
        baDecrypted1 = cipher.doFinal()
        var res = baDecrypted1.toString(StandardCharsets.UTF_8)
        assert(res == (s1+s2+s3))

        return
    }

    @Test
    fun testRSABouncyCastleEncryptDecrypt() {
        //var rsaPublicKey1 = RSAPublicKey(publicKey1)
        //var algorithm = Algorithm.RSA256(publicKey1, privateKey1)

    }

    @Test
    fun testSecretKeyGeneration() {
        var password = "mypassword"
        var salt = "my8BSalt"

        // method 1
        var keyGenerator = KeyGenerator.getInstance("AES")
        var sz = 128 // 128,192,256
        keyGenerator.init(sz)
        var secretKey = keyGenerator.generateKey()
        var sEncodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        var baDecodedKey = Base64.getDecoder().decode(sEncodedKey)
        var originKey = SecretKeySpec(baDecodedKey, 0, baDecodedKey.size, "AES")
        assert(originKey.equals(secretKey))

        // method 2
        var iterationCount = 8000
        var keyLength = 256
        // PBKDF2WithHmacSHA256 in BouncyCastle is PKCS5S2ParametersGenerator
        var secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        var keySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterationCount, keyLength)
        secretKey = secretKeyFactory.generateSecret(keySpec)
        SecretKeySpec(secretKey.encoded, "AES")

        keySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterationCount, 512)

        //var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        var algorithmParameters = cipher.parameters
        //var ba = algorithmParameters.getParameterSpec<ByteArray>(IvParameterSpec.class).getIV()
    }

    @Test
    fun testBouncyCastleLoad() {
        Security.addProvider(BouncyCastleProvider())
        val certFactory = CertificateFactory.getInstance("X.509", "BC")
        val certificate = certFactory.generateCertificate(FileInputStream("test.cer")) as X509Certificate
        val keystore = KeyStore.getInstance("PKCS12")
        val keystorePassword = "password"
        val keyPassword = "password"
        keystore.load(FileInputStream("test.p12"), keystorePassword.toCharArray())
        val privateKey = keystore.getKey("testalias", keyPassword.toCharArray())
    }
    /*
     * HMac process
     *
     *  - create secret key or asymmetric key
     *      - symmetric
     *          - AES, HMACSHA512, RC2
     *  - generate MAC
     *      - HMACSHA256/512
     *  - salt via SecureRandom generate
     */
    @Test
    fun testHmacAuth0() {

    }

    @Test
    fun testHMacJJWT() {
        val s1 = "The quick brown fox jumps over the lazy dog"
        val s2 = "The cat in the hat is like a rat in a mat"

        var keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        var secretKey = keyGenerator.generateKey()

        var hmac = Mac.getInstance("HmacSHA256")
        hmac.init(secretKey)
        hmac.update(s1.toByteArray())
        var signedBytes = hmac.doFinal(s2.toByteArray())

        var signatureAlgorithm = SignatureAlgorithm.HS256
        // this is jjwt
        var signer = MacSigner(signatureAlgorithm, secretKey.encoded)
        //MacValidator

        val jsonObject = JSONObject()
        // this is from io.jsonwebtoken.com github jwtk/jjwt
        // maven says this has dependency vulnerabilities since 07-2018
        // so dont use jjwt? use auth0 instead?
        // val bearerToken = JWebToken(jsonObject).toString()
    }

    @Test
    fun testJSONObject() {
        var objectMapper = ObjectMapper()
        var headers = mutableMapOf<String, Any>()
        headers["typ"] = "JWT"
        headers["alg"] = "HS256"
        headers["iss"] = "issuer"
        headers["sub"] = "subject"
        headers["aud"] = listOf("audience1","audience2")
        headers["at"] = 12345
        headers["exp"] = 12346
        headers["nonce"] = "nonce"
        var payload = mapOf<String, Any>(
            "data1" to "The quick brown fox jumped over the lazy dog",
            "data2" to "The cat in the hat is from a book"
        )
        var headerString = objectMapper.writeValueAsString(headers)
        var payloadString = objectMapper.writeValueAsString(payload)
        var headerb64 = Base64.getUrlEncoder().encode(headerString.toByteArray()).toString(StandardCharsets.UTF_8)
        var payloadb64 = Base64.getUrlEncoder().encode(payloadString.toByteArray()).toString(StandardCharsets.UTF_8)

        println("header:$headerb64")
        println("payload:$payloadb64")

        var expHeader = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImlzcyI6Imlzc3VlciIsInN1YiI6InN1YmplY3QiLCJhdWQiOlsiYXVkaWVuY2UxIiwiYXVkaWVuY2UyIl0sImF0IjoxMjM0NSwiZXhwIjoxMjM0Niwibm9uY2UiOiJub25jZSJ9"
        var expPayload = "eyJkYXRhMSI6IlRoZSBxdWljayBicm93biBmb3gganVtcGVkIG92ZXIgdGhlIGxhenkgZG9nIiwiZGF0YTIiOiJUaGUgY2F0IGluIHRoZSBoYXQgaXMgZnJvbSBhIGJvb2sifQ=="
        assert(headerb64 == expHeader)
        assert(payloadb64 == expPayload)

        headers = mutableMapOf<String, Any>()
        headers["typ"] = "JWT"
        headers["alg"] = "RS256"
        headers["iss"] = "issuer"
        headers["sub"] = "subject"
        headers["aud"] = listOf("audience1","audience2")
        headers["at"] = 12345
        headers["exp"] = 12346
        headers["nonce"] = "nonce"
        headerString = objectMapper.writeValueAsString(headers)
        headerb64 = Base64.getUrlEncoder().encode(headerString.toByteArray()).toString(StandardCharsets.UTF_8)
        println("header:$headerb64")
        println("payload:$payloadb64")

        expHeader = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImlzcyI6Imlzc3VlciIsInN1YiI6InN1YmplY3QiLCJhdWQiOlsiYXVkaWVuY2UxIiwiYXVkaWVuY2UyIl0sImF0IjoxMjM0NSwiZXhwIjoxMjM0Niwibm9uY2UiOiJub25jZSJ9"
        expPayload = "eyJkYXRhMSI6IlRoZSBxdWljayBicm93biBmb3gganVtcGVkIG92ZXIgdGhlIGxhenkgZG9nIiwiZGF0YTIiOiJUaGUgY2F0IGluIHRoZSBoYXQgaXMgZnJvbSBhIGJvb2sifQ=="
        assert(headerb64 == expHeader)
        assert(payloadb64 == expPayload)
    }

    @Test
    fun testJWTAuth0RSA1() {
        var publicKeyString = """

        """.trimIndent().replace("\n","")
        var privateKeyString = """

        """.trimIndent().replace("\n","")

        println("publicKey: $publicKeyString")
        println("privateKey: $privateKeyString")
        var header = ""
        var payload = ""

        var keyFactory = KeyFactory.getInstance("RSA")
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair: KeyPair = keyPairGenerator.genKeyPair()
        var publicKey: PublicKey = keyPair.public
        var privateKey: PrivateKey = keyPair.private

        // var rsaKeyProvider = RSAKeyProvider()
        // // this doesnt work
        // var algorithm = Algorithm.RSA256(rsaKeyProvider)

        // var signatureBA = algorithm.sign(header.toByteArray(), payload.toByteArray())
        // var signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBA)

        // var jwt = "$header.$payload.$signature"

        // var decodedJWT = JWT.decode(jwt)
        // algorithm.verify(decodedJWT)
    }
    @Test
    fun testJWTAuth0PublicPrivateKeyNotWorking() {
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // use 4096 or 8192 in prod
        var keyPair: KeyPair = keyPairGenerator.genKeyPair()
        var publicKey: PublicKey = keyPair.public
        var privateKey: PrivateKey = keyPair.private
        var publicKeyString = """

        """.trimIndent().replace("\n","")
        var privateKeyString = """

        """.trimIndent().replace("\n","")

        println("publicKey: $publicKeyString")
        println("privateKey: $privateKeyString")
        var header = ""
        var payload = ""

        // this doesnt work
        var algorithmPrivate = Algorithm.HMAC256(privateKey.encoded)
        var algorithmPublic = Algorithm.HMAC256(publicKey.encoded)
        algorithmPrivate = Algorithm.HMAC256(Base64.getDecoder().decode(privateKeyString))
        algorithmPublic = Algorithm.HMAC256(Base64.getDecoder().decode(publicKeyString))

        var signatureBA = algorithmPrivate.sign(header.toByteArray(), payload.toByteArray())
        var signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBA)

        var jwt = "$header.$payload.$signature"

        var decodedJWT = JWT.decode(jwt)
        algorithmPrivate.verify(decodedJWT)
        algorithmPublic.verify(decodedJWT)

    }
    /*
     * JWT has 3 parts
     * - header
     * - payload
     * - signature
     */
    @Test
    fun testJWTAuth0HmacSHA256() {
        var objectMapper = ObjectMapper()
        var secret = "secret"
        var sAlgorithm = "HmacSHA256"
        var algorithm = Algorithm.HMAC256(secret)
        var headerClaims = mutableMapOf<String, Any>()
        headerClaims["iss"] = "issuer"
        headerClaims["sub"] = "subject"
        headerClaims["aud"] = listOf("audience1","audience2")
        headerClaims["at"] = 12345
        headerClaims["exp"] = 12346
        headerClaims["nonce"] = "nonce"
        var payloadClaims = mapOf<String, Any>(
            "data1" to "The quick brown fox jumped over the lazy dog",
            "data2" to "The cat in the hat is from a book"
        )
        var headerString = objectMapper.writeValueAsString(headerClaims)
        var payloadString = objectMapper.writeValueAsString(payloadClaims)
        var headerb64 = Base64.getUrlEncoder().encode(headerString.toByteArray()).toString(StandardCharsets.UTF_8)
        var payloadb64 = Base64.getUrlEncoder().encode(payloadString.toByteArray()).toString(StandardCharsets.UTF_8)
        var jwt: String = JWT.create()
            .withHeader(headerClaims)
            .withPayload(payloadClaims)
            .sign(algorithm)

        var decodedJWT: DecodedJWT = JWT.decode(jwt)
        var header = Base64.getUrlDecoder().decode(decodedJWT.header).toString(StandardCharsets.UTF_8)
        var payload = Base64.getUrlDecoder().decode(decodedJWT.payload).toString(StandardCharsets.UTF_8)
        var signature = Base64.getUrlDecoder().decode(decodedJWT.signature)

        // validate signature
        algorithm.verify(decodedJWT)
        val mac = Mac.getInstance(sAlgorithm)
        mac.init(SecretKeySpec(secret.toByteArray(), sAlgorithm))
        mac.update(decodedJWT.header.toByteArray())
        mac.update(".".toByteArray())
        var baCheck: ByteArray = mac.doFinal(decodedJWT.payload.toByteArray())
        assert(signature.contentEquals(baCheck))

        var sHeader = """{"sub":"subject","aud":["audience1","audience2"],"at":12345,"iss":"issuer","typ":"JWT","exp":12346,"nonce":"nonce","alg":"HS256"}"""
        var sPayload = """{"data2":"The cat in the hat is from a book","data1":"The quick brown fox jumped over the lazy dog"}"""

        assert(objectMapper.readTree(sHeader) == objectMapper.readTree(header))
        assert(objectMapper.readTree(sPayload) == objectMapper.readTree(payload))
    }
    @Test
    fun testJWTVerifyDiscard() {
        var secret: String?
        secret = ""
        var algorithm = Algorithm.HMAC256(secret)
        var jwt: String?
        jwt = ""

        var decodedJWT: DecodedJWT = JWT.decode(jwt)
        var header = Base64.getUrlDecoder().decode(decodedJWT.header).toString(StandardCharsets.UTF_8)
        var payload = Base64.getUrlDecoder().decode(decodedJWT.payload).toString(StandardCharsets.UTF_8)
        var signature = Base64.getUrlDecoder().decode(decodedJWT.signature)
        algorithm.verify(decodedJWT)

    }
    @Test
    fun testJWTEncodeStringEmpty() {
        var jwtString = ""

        var header = ""
        var payload = ""
        var expectedSignature = ""

        var secrets = listOf(
            "[]",
            "password",
            "test"
        )

        println("exp sign:  $expectedSignature")
        for(secret in secrets) {
            var signature = getJWTEncodeHS256Signature(secret, header, payload)
            println("signature: $signature secret: $secret")
        }
    }
    fun getJWTEncodedRS256Signature(rsaKeyProvider: RSAKeyProvider, header: String, payload: String): String {
        var algorithm = Algorithm.RSA256(rsaKeyProvider)
        var signatureBytes = algorithm.sign(header.toByteArray(), payload.toByteArray())
        var signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)
        return signature
    }
    fun getJWTEncodeHS256Signature(secret: String, header: String, payload: String): String {
        var algorithm = Algorithm.HMAC256(secret)
        var signatureBytes: ByteArray = algorithm.sign(header.toByteArray(), payload.toByteArray())
        //var signatureBytes: ByteArray = algorithm.sign(header.toByteArray(StandardCharsets.UTF_8), payload.toByteArray(StandardCharsets.UTF_8))
        var signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)
        return signature
    }
    fun getJWTEncodeHS256(secret: String, header: String, payload: String): String {
        var signature = getJWTEncodeHS256Signature(secret, header, payload)
        var jwt = "$header.$payload.$signature"
        return jwt
    }
    @Test
    fun testJWTDiscard() {
    }
    @Test
    fun testJWTAuth0Detailed() {
        var objectMapper = ObjectMapper()
        var secret = "secret"
        var sAlgorithm = "HmacSHA256"
        var algorithm = Algorithm.HMAC256(secret)
        var headerClaims = mutableMapOf<String, Any>()
        headerClaims["iss"] = "issuer"
        headerClaims["sub"] = "subject"
        headerClaims["aud"] = listOf("audience1","audience2")
        headerClaims["at"] = 12345
        headerClaims["exp"] = 12346
        headerClaims["nonce"] = "nonce"
        headerClaims["typ"] = "JWT"
        headerClaims["alg"] = "HS256"
        var payloadClaims = mutableMapOf<String, Any>()
        payloadClaims["data1"] = "The quick brown fox jumped over the lazy dog"
        payloadClaims["data2"] = "The cat in the hat is from a book"

        // the ordering changed... cannot use objectMapper
        var headerClaimsHardcode = """{"sub":"subject","aud":["audience1","audience2"],"at":12345,"iss":"issuer","typ":"JWT","exp":12346,"nonce":"nonce","alg":"HS256"}"""
        var payloadClaimsHardcode = """{"data2":"The cat in the hat is from a book","data1":"The quick brown fox jumped over the lazy dog"}"""
        var headerClaimsStr = objectMapper.writeValueAsString(headerClaims)
        var payloadClaimsStr = objectMapper.writeValueAsString(payloadClaims)

        headerClaimsStr = headerClaimsHardcode
        payloadClaimsStr = payloadClaimsHardcode

        var b64HeaderString = Base64.getUrlEncoder().withoutPadding().encodeToString(headerClaimsStr.toByteArray())
        var b64PayloadString = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadClaimsStr.toByteArray())
        var signatureBA = algorithm.sign(b64HeaderString.toByteArray(StandardCharsets.UTF_8), b64PayloadString.toByteArray(StandardCharsets.UTF_8))
        var b64Signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBA)
        var jwt1 = "$b64HeaderString.$b64PayloadString.$b64Signature"

        var jwt2: String = JWT.create()
            .withHeader(headerClaims)
            .withPayload(payloadClaims)
            .sign(algorithm)

        assert(jwt1 == jwt2)

        var jwt = jwt2

        var sjwtHeader = "eyJzdWIiOiJzdWJqZWN0IiwiYXVkIjpbImF1ZGllbmNlMSIsImF1ZGllbmNlMiJdLCJhdCI6MTIzNDUsImlzcyI6Imlzc3VlciIsInR5cCI6IkpXVCIsImV4cCI6MTIzNDYsIm5vbmNlIjoibm9uY2UiLCJhbGciOiJIUzI1NiJ9"
        var sjwtPayload = "eyJkYXRhMiI6IlRoZSBjYXQgaW4gdGhlIGhhdCBpcyBmcm9tIGEgYm9vayIsImRhdGExIjoiVGhlIHF1aWNrIGJyb3duIGZveCBqdW1wZWQgb3ZlciB0aGUgbGF6eSBkb2cifQ"
        var sjwtSignature = "bwWj11iDU3SlMFeIM363p70qKShYRFbd4RWg68LICvw"
        var sjwt = "$sjwtHeader.$sjwtPayload.$sjwtSignature"
        assert(jwt == sjwt)

        var decodedJWT: DecodedJWT = JWT.decode(jwt)
        assert(decodedJWT.header == sjwtHeader)
        assert(decodedJWT.payload == sjwtPayload)
        assert(decodedJWT.signature == sjwtSignature)

        // use Base64.getUrlDecoder(), not Base64.getDecoder(),
        var header = Base64.getUrlDecoder().decode(decodedJWT.header).toString(StandardCharsets.UTF_8)
        var payload = Base64.getUrlDecoder().decode(decodedJWT.payload).toString(StandardCharsets.UTF_8)
        var signature = Base64.getUrlDecoder().decode(decodedJWT.signature)
        println(header)
        println(payload)

        // validate signature
        algorithm.verify(decodedJWT)

        // validate HMAC with decoded header and payload (not the Base64 decoded!)
        val mac = Mac.getInstance(sAlgorithm)
        mac.init(SecretKeySpec(secret.toByteArray(), sAlgorithm))
        mac.update(decodedJWT.header.toByteArray())
        mac.update(".".toByteArray())
        var baCheck: ByteArray = mac.doFinal(decodedJWT.payload.toByteArray())
        assert(signature.contentEquals(baCheck))

        var jsonHeader = JSONObject(header)
        var jsonPayload = JSONObject(payload)

        println("jsonHeader:${jsonHeader.toString()}")
        println("jsonPayload:${jsonPayload.toString()}")

        var sHeader = """{"sub":"subject","aud":["audience1","audience2"],"at":12345,"iss":"issuer","typ":"JWT","exp":12346,"nonce":"nonce","alg":"HS256"}"""
        var sPayload = """{"data2":"The cat in the hat is from a book","data1":"The quick brown fox jumped over the lazy dog"}"""
        var jsonHeaderRef = JSONObject(sHeader)
        var jsonPayloadRef = JSONObject(sPayload)
        assert(jsonHeader.toString() == sHeader)
        assert(jsonPayload.toString() == sPayload)

        assert(objectMapper.readTree(sHeader) == objectMapper.readTree(header))
        assert(!jsonHeaderRef.equals(jsonHeader))
        assert(objectMapper.readTree(sPayload) == objectMapper.readTree(payload))
        assert(!jsonPayloadRef.equals(jsonPayload))

        // JWTCreator.init() is package private
        // JWTCreator.init()
        //     .withHeader(headerClaims)
        //     .sign(algorithm)
        return
    }
    @Test
    fun testJWTWithNoneAlgos() {
        var objectMapper = ObjectMapper()
        var secret = "secret"
        var sAlgorithm = "none"
        var algorithm = Algorithm.none()
        var headerClaims = mutableMapOf<String, Any>()
        headerClaims["iss"] = "issuer"
        headerClaims["sub"] = "subject"
        headerClaims["aud"] = listOf("audience1","audience2")
        headerClaims["at"] = 12345
        headerClaims["exp"] = 12346
        headerClaims["nonce"] = "nonce"
        var payloadClaims = mapOf<String, Any>(
            "data1" to "The quick brown fox jumped over the lazy dog",
            "data2" to "The cat in the hat is from a book"
        )
        var jwt: String = JWT.create()
            .withHeader(headerClaims)
            .withPayload(payloadClaims)
            .sign(algorithm)

        var decodedJWT: DecodedJWT = JWT.decode(jwt)
        var header = Base64.getUrlDecoder().decode(decodedJWT.header).toString(StandardCharsets.UTF_8)
        var payload = Base64.getUrlDecoder().decode(decodedJWT.payload).toString(StandardCharsets.UTF_8)
        var signature = Base64.getUrlDecoder().decode(decodedJWT.signature)

        // validate signature
        algorithm.verify(decodedJWT)
        val mac = Mac.getInstance(sAlgorithm)
        mac.init(SecretKeySpec(secret.toByteArray(), sAlgorithm))
        mac.update(decodedJWT.header.toByteArray())
        mac.update(".".toByteArray())
        var baCheck: ByteArray = mac.doFinal(decodedJWT.payload.toByteArray())
        assert(signature.contentEquals(baCheck))

        var sHeader = """{"sub":"subject","aud":["audience1","audience2"],"at":12345,"iss":"issuer","typ":"JWT","exp":12346,"nonce":"nonce","alg":"HS256"}"""
        var sPayload = """{"data2":"The cat in the hat is from a book","data1":"The quick brown fox jumped over the lazy dog"}"""

        assert(objectMapper.readTree(sHeader) == objectMapper.readTree(header))
        assert(objectMapper.readTree(sPayload) == objectMapper.readTree(payload))
    }
    @Test
    fun testJJWTPrivateKey() {
        var keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        var secretKey = keyGenerator.generateKey()

        // PrivateKey privateKey
        var signatureAlgorithm = SignatureAlgorithm.HS256
        signatureAlgorithm.getJcaName()
        var jwtBuilder = Jwts.builder()
            .setId("id")
            .setIssuedAt(Date.from(Instant.now()))
            .setSubject("subject")
            .setIssuer("issuer")
            .signWith(signatureAlgorithm, secretKey)

        // decode token
        var sjwt = ""
        var claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(sjwt).body

    }
    @Test
    fun testJWTShared() {
        val secret = "secret";
        val clientId = "clientId"
        val now = Instant.now()
        val jwt: String = Jwts.builder()
            .setAudience("https://securesite.com")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(5L, ChronoUnit.MINUTES)))
            .setIssuer(clientId)
            .setSubject(clientId)
            .setId(UUID.randomUUID().toString())
            //.signWith(secret) // can use string, key, or byteArray
            .compact()
    }
    @Test
    fun testSecureRandom() {
        var secureRandom: SecureRandom = SecureRandom()
        secureRandom.nextInt()
        secureRandom.nextInt(0x7fff_ffff)
        secureRandom = SecureRandom.getInstance("AES")
    }
    @Test
    fun testJSONSerdes() {
        val objectMapper = ObjectMapper()
    }
}