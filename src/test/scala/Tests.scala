import org.scalatest.FunSuite

import com.github.salva.scala.glob._

class Tests extends FunSuite {
  def testResult(glob:String, path:String, cI:Boolean, p:Boolean, result:MatchResult) = {
    test("glob " + glob + " ~ " + path +
      (if (cI) " [CI]" else "") +
      (if (p) "[P]" else "") +
      " --> " + result) {
      assert(new Glob(glob, cI).matches(path) == result)
    }
  }
  def testNoMatch(glob:String, path:String, cI:Boolean=false, p:Boolean=false) =
    testResult(glob, path, p, cI, NoMatch)
  def testMatch(glob:String, path:String, cI:Boolean=false, p:Boolean=false) =
    testResult(glob, path, cI, p, Match(false))
  def testMatchDir(glob:String, path:String, cI:Boolean=false, p:Boolean=false) =
    testResult(glob, path, cI, p, Match(true))

  testMatch("/etc", "/etc")
  testMatch("/etc", "/etc/")
  testMatch("/etc", "/etc//")
  testMatch("/e[t]c", "/etc")
  testMatch("/?tc", "/etc")
  testMatch("/*tc", "/tc")
  testMatch("/*tc", "/etc")
  testMatch("/*tc", "/eetc")
  testMatch("/[a-z]tc", "/etc")
  testMatch("/[a-cd-g]tc", "/etc/")
  testMatch("/etc**", "/etc")
  testMatch("/etc**", "/etc/")
  testMatch("/etc**", "/etc/foo")
  testMatch("/etc**", "/etc/foo/")
  testMatch("/etc**", "/etc/foo/bar")
  testMatch("/etc**", "/etcfoo/bar")
  testMatch("/etc**", "/etc//foo")
  testMatch("/etc**", "/etcfoo//foo")
  testMatch("/etc**", "/etcfoo//foo/")
  testMatch("/etC**", "/EtcfOO//foo/", true)
  testMatch("/et*", "/etc")
  testMatch("/e*", "/etc")
  testMatch("/*t?", "/etc/")
  testMatch("/???", "/etc")
  testMatch("???", "etc")
  testMatch("{foo,bar,doz}/**/*.jp{,e}g", "bar/foo/000/000000_211.jpg")
  testMatch("{foo,bar,doz}/**/*.jp{,e}g", "foo/foo/000/000000_211.jpeg")

  testMatchDir("/etc/", "/etc")
  testMatchDir("/etc/", "/etc/")
  testMatchDir("/etc**/", "/etc")
  testMatchDir("/etc**/bar/", "/etcfoo///dii/bar")
  testMatchDir("/???/", "/etc")

  testNoMatch("/etc", "etc")
  testNoMatch("/etc", "etc/")
  testNoMatch("/etc", "/")
  testNoMatch("/etc", ".")
  testNoMatch("/etc", "./etc")
  testNoMatch("/[A-Z]tc", "/etc")
  testNoMatch("/[!a-z]tc", "/etc")
  testNoMatch("/?tc", "/eetc")
  testNoMatch("/etC**", "/EtcfOO//foo/", false)
  testNoMatch("/????", "/.etc")
  testNoMatch("????", ".etc")
  testNoMatch("/?", "/.")
  testNoMatch("/??", "/..")
  testNoMatch("/*", "/.")
  testNoMatch("/*", "/..")
  testNoMatch("/**", "/.")
  testNoMatch("/**", "/..")
  testNoMatch("**", "/..")
  testNoMatch("**", "foo/..")
  testNoMatch("**", "foo/../bar")
  testNoMatch("*/**t", "apt")
  /* matchingPartially */

  def testPartialResult(glob:String, path:String, cI:Boolean, p:Boolean, result:MatchResult) = {
    test("glob partial " + glob + " ~ " + path +
      (if (cI) " [CI]" else "") +
      (if (p) " [P]" else "") +
      " --> " + result) {
      assert(new Glob(glob, cI, p).matchesPartially(path) == result)
    }
  }
  def testPartialNoMatch(glob:String, path:String, cI:Boolean=false, p:Boolean=false) = testPartialResult(glob, path, cI, p, NoMatch)
  def testPartialMatchDir(glob:String, path:String, cI:Boolean=false, p:Boolean=false) = testPartialResult(glob, path, cI, p, Match(true))

  testPartialMatchDir("/etc/", "/")
  testPartialMatchDir("etc", "")
  testPartialMatchDir("/etc**", "/etc")
  testPartialMatchDir("/etc**", "/etc///")
  testPartialMatchDir("/etc**", "/etcbar/foo")
  testPartialMatchDir("/etc/**", "/etc")
  testPartialMatchDir("/etc/**", "/etc/foo")
  testPartialMatchDir("/etc/**", "/etc/foo///")
  testPartialMatchDir("/Etc/**", "/etC/foo///", true)
  testPartialMatchDir("/etc/.**", "/etc/.foo")
  testPartialMatchDir("/etc/.**", "/etc/")
  testPartialMatchDir("/etc/.**", "/etc/..", p=true)
  testPartialMatchDir("/etc/.**", "/etc/.", p=true)
  testPartialMatchDir("/etc/.**", "/etc/..", p=false)
  testPartialMatchDir("/etc/.**", "/etc/.", p=false)
  testPartialMatchDir("/etc/**", "/etc/.foo", p=true)
  testPartialMatchDir("/etc/**", "/etc/..foo.", p=true)
  testPartialMatchDir("/etc/{foo,bar}**", "/")
  testPartialMatchDir("/etc/{foo,bar}**", "/etc")
  testPartialMatchDir("/etc/{foo,bar}**", "/etc/")
  testPartialMatchDir("/etc/{foo,bar}**", "/etc/foo")
  testPartialMatchDir("/etc/{foo,bar}**", "/etc/bar")
  testPartialMatchDir("/etc/{foo,bar}**", "/etc/barfoo")
  testPartialMatchDir("/etc/{foo,bar}**", "/etc/bar/foo")
  testPartialMatchDir("**/etc/", "/etc")
  testPartialMatchDir("**/etc/", "/foo/etc")
  testPartialMatchDir("**/etc", "/foo/etc")
  testPartialMatchDir("/[a-z]tc/foo", "/etc/")
  testPartialMatchDir("/?tc/foo", "/etc/")
  testPartialMatchDir("/*tc/foo", "/etc/")
  testPartialMatchDir("/*tc/foo", "/.etc", p=true)
  testPartialMatchDir("//*tc//foo", "/.etc", p=true)
  testPartialMatchDir("//*tc//foo", "//.etc", p=true)
  testPartialMatchDir("/*tc//foo", "//.etc", p=true)

  testPartialNoMatch("/etc", "/etc")
  testPartialNoMatch("etc", "/")
  testPartialNoMatch("/etc", "/usr")
  testPartialNoMatch("/etc/", "/etc")
  testPartialNoMatch("/etc", "etc")
  testPartialNoMatch("etc", "etc")
  testPartialNoMatch("/Etc/**", "/etC/foo///", false)
  testPartialNoMatch("/etc/**", "/etc/..", p=true)
  testPartialNoMatch("/etc/**", "/etc/.", p=true)
  testPartialNoMatch("/etc/**", "/etc/../etc", p=true)
  testPartialNoMatch("/etc/**", "/etc/./etc", p=true)
  testPartialNoMatch("/etc/**", "/etc/.foo", p=false)
  testPartialNoMatch("/etc/**", "/etc/..foo.", p=false)
  testPartialNoMatch("/etc/{foo,bar}**", "/etc/doz")
  testPartialNoMatch("/[a-z]tc/foo", "/etc/foo")
  testPartialNoMatch("/?tc/foo", "/etc/foo")
  testPartialNoMatch("/??tc/foo", "/etc")
  testPartialNoMatch("/*tc/foo", "/.etc")
  testPartialNoMatch("//*tc//foo", "/.etc")





}
