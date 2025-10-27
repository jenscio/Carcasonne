package ch.epfl.chacun;
/**
 * Base32:
 * contient les méthodes permettant d'encoder et de décoder des valeurs binaires en base32
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */
public final class Base32 {
    /**
     * Constructeur vide (classe non instantiable)
     */
    private Base32() {}
    // chaîne contenant les caractères correspondant aux chiffres en base 32, ordonnés par poids croissant
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * verifie si la chaine en argument n'est composée que de caractères de l'alphabet base32
     * @param base32String : la chaine en question
     * @return  vrai ssi la chaine n'est composée que de caractères de l'alphabet base32
     */
    public static boolean isValid(String base32String) {
        if(base32String == null || base32String.isBlank()) return false;
        for (int i = 0; i < base32String.length(); i++) {
            if (!ALPHABET.contains(Character.toString(base32String.charAt(i)))) {
                return false;
            }
        }
        return true;
    }
    /**
     * retourne la chaîne de longueur 1 correspondant à l'encodage en base32 des 5 bits de
     *  poids faible du int en parametre
     * @param toEncode : parametre à encoder
     * @return la chaîne encodée
     * @throws IllegalArgumentException: Si le paramètre n'est pas valable
     */
    public static String encodeBits5(int toEncode) {
        Preconditions.checkArgument(toEncode>=0 && toEncode<=31);
        return String.valueOf(ALPHABET.charAt((toEncode & 0b11111)));
    }
    /**
     * retourne la chaîne de longueur 2 correspondant à l'encodage en base32 des 10 bits de poids
     *  faible du int en parametre
     * @param toEncode : parametre à encoder
     * @return la chaîne encodée
     */
    public static String encodeBits10(int toEncode) {
        Preconditions.checkArgument(toEncode >= 0 && toEncode <= 1023);
        return encodeBits5(toEncode >> 5) + encodeBits5(toEncode & 0b11111);
    }

    /**
     * prends en argument une chaîne de longueur 1 ou 2 représentant un nombre en base32 et retournant
     *  l'entier de type int correspondant.
     * @param base32String : la chaine
     * @return : l'entier de type int correspondant
     */
    public static int decode(String base32String) {
        Preconditions.checkArgument(isValid(base32String) && base32String.length()<=2);
        int result = ALPHABET.indexOf(base32String.charAt(0));
        if (base32String.length() == 2) {
            result = result * 32 + ALPHABET.indexOf(base32String.charAt(1));
        }
        return result;
    }
}
