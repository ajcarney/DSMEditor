package Matrices.Views.Flags;


/**
 * Interface for supporting validate symmetry highlighting
 */
public interface ISymmetricHighlight {
    /**
     * Sets symmetryValidation to true in order to highlight symmetry errors
     */
    void setValidateSymmetry();


    /**
     * Sets symmetryValidation to false in order to stop highlighting symmetry errors
     */
    void clearValidateSymmetry();


    /**
     * @return  if validation symmetry is set or not
     */
    boolean getSymmetryValidation();
}
