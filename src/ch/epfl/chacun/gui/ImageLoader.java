package ch.epfl.chacun.gui;

import javafx.scene.image.Image;

/**
 * ImageLoader:
 * charge les images des tuiles
 *
 * @author Rayan Berrada (378940)
 * @author Jens Cancio (379510)
 */


public final class ImageLoader {

    /**
     * Constructeur vide (classe non instantiable)
     */
    private ImageLoader() {
    }

    public final static int LARGE_TILE_PIXEL_SIZE = 512; // Taille des grandes tuiles
    public final static int LARGE_TILE_FIT_SIZE = 256; //  Taille d'affichage des grandes tuiles
    public final static int NORMAL_TILE_PIXEL_SIZE = 256; //    Taille des tuiles normales
    public final static int NORMAL_TILE_FIT_SIZE = 128; //  Taille d'affichage des tuiles normales
    public final static int MARKER_PIXEL_SIZE = 96; //  Taille du marqueur
    public final static int MARKER_FIT_SIZE = 48; //   Taille d'affichage du marqueur

    // repertoire des images des tuiles grandes
    private final static String PATH_TO_LARGE_TILE_IMAGES = "/" + LARGE_TILE_PIXEL_SIZE + "/";
    // repertoire des images des tuiles normales
    private final static String PATH_TO_NORMAL_TILE_IMAGES = "/" + NORMAL_TILE_PIXEL_SIZE + "/";


    /**
     * retourne l'image de la tuile correspondante aux parametres
     *
     * @param tileId:   id de la tuile
     * @param path:     chemin d'accès
     * @param fit_size: taille en pixels a visualizer de l'image
     * @return image de la tuile
     */
    private static Image imageForTile(int tileId, String path, int fit_size) {
        String tileIdString = tileId >= 10 ? String.valueOf(tileId) : "0" + tileId;
        return new Image(path + tileIdString + ".jpg", fit_size, fit_size, true, true);
    }

    /**
     * retourne l'image de 256 pixels de côté de la face de la tuile donnée
     *
     * @param tileId: id de la tuile
     * @return l'image de 256 pixels de côté de la face de la tuile donnée
     */
    public static Image normalImageForTile(int tileId) {
        return imageForTile(tileId, PATH_TO_NORMAL_TILE_IMAGES, NORMAL_TILE_FIT_SIZE);
    }

    /**
     * retourne l'image de 512 pixels de côté de la face de la tuile donnée
     *
     * @param tileId: id de la tuile
     * @return l'image de 256 pixels de côté de la face de la tuile donnée
     */
    public static Image largeImageForTile(int tileId) {
        return imageForTile(tileId, PATH_TO_LARGE_TILE_IMAGES, LARGE_TILE_FIT_SIZE);
    }

}
