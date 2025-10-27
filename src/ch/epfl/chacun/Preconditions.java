package ch.epfl.chacun;
/**
 *  Preconditioms :
 *  permet de faciliter l'écriture de préconditions
 *  non instanciables
 *
 *  @author Rayan Berrada (378940)
 *  @author Jens Cancio (379510)
 *
 */

public final class Preconditions {
    /**
     * constructeur privé, rends impossible la création d'instances de la classe
     */
    private Preconditions() {}
    /**
     * methode publique et statique
     * @param shouldBeTrue: booléen passé en paramètre
     * @throws IllegalArgumentException: lève l'exception IllegalArgumentException si son argument est faux, et ne fait rien sinon
     */
    public static void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue){
            throw new IllegalArgumentException();
        }
    }
}


