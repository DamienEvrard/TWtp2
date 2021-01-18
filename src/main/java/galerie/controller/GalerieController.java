package galerie.controller;

import galerie.dao.ArtisteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import galerie.dao.GalerieRepository;
import galerie.dao.TableauRepository;
import galerie.entity.Artiste;
import galerie.entity.Galerie;
import galerie.entity.Tableau;
import java.util.ArrayList;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Edition des catégories, sans gestion des erreurs
 */
@Controller
@RequestMapping(path = "/galerie")
public class GalerieController {

    @Autowired
    private GalerieRepository gdao;
    @Autowired
    private TableauRepository tdao;
    @Autowired
    private ArtisteRepository adao;

    /**
     * Affiche toutes les catégories dans la base
     *
     * @param model pour transmettre les informations à la vue
     * @return le nom de la vue à afficher ('afficheGaleries.html')
     */
    @GetMapping(path = "show")
    public String afficheToutesLesGaleries(Model model) {
        model.addAttribute("galeries", gdao.findAll());
        return "afficheGaleries";
    }

    /**
     * Montre le formulaire permettant d'ajouter une galerie
     *
     * @param galerie initialisé par Spring, valeurs par défaut à afficher dans le formulaire
     * @return le nom de la vue à afficher ('formulaireGalerie.html')
     */
    @GetMapping(path = "add")
    public String montreLeFormulairePourAjout(@ModelAttribute("galerie") Galerie galerie) {
        return "formulaireGalerie";
    }

    /**
     * Appelé par 'formulaireGalerie.html', méthode POST
     *
     * @param galerie Une galerie initialisée avec les valeurs saisies dans le formulaire
     * @param redirectInfo pour transmettre des paramètres lors de la redirection
     * @return une redirection vers l'affichage de la liste des galeries
     */
    @PostMapping(path = "save")
    public String ajouteLaGaleriePuisMontreLaListe(Galerie galerie, RedirectAttributes redirectInfo) {
        String message;
        try {
            // cf. https://www.baeldung.com/spring-data-crud-repository-save
            gdao.save(galerie);
            // Le code de la catégorie a été initialisé par la BD au moment de l'insertion
            message = "La galerie '" + galerie.getNom() + "' a été correctement enregistrée";
        } catch (DataIntegrityViolationException e) {
            // Les noms sont définis comme 'UNIQUE' 
            // En cas de doublon, JPA lève une exception de violation de contrainte d'intégrité
            message = "Erreur : La galerie '" + galerie.getNom() + "' existe déjà";
        }
        // RedirectAttributes permet de transmettre des informations lors d'une redirection,
        // Ici on transmet un message de succès ou d'erreur
        // Ce message est accessible et affiché dans la vue 'afficheGalerie.html'
        redirectInfo.addFlashAttribute("message", message);
        return "redirect:show"; // POST-Redirect-GET : on se redirige vers l'affichage de la liste		
    }

    /**
     * Appelé par le lien 'Supprimer' dans 'afficheGaleries.html'
     *
     * @param galerie à partir de l'id de la galerie transmis en paramètre, Spring fera une requête SQL SELECT pour
     * chercher la galerie dans la base
     * @param redirectInfo pour transmettre des paramètres lors de la redirection
     * @return une redirection vers l'affichage de la liste des galeries
     */
    @GetMapping(path = "delete")
    public String supprimeUneCategoriePuisMontreLaListe(@RequestParam("id") Galerie galerie, RedirectAttributes redirectInfo) {
        String message = "La galerie '" + galerie.getNom() + "' a bien été supprimée";
        try {
            gdao.delete(galerie); // Ici on peut avoir une erreur (Si il y a des expositions pour cette galerie par exemple)
        } catch (DataIntegrityViolationException e) {
            // violation de contrainte d'intégrité si on essaie de supprimer une galerie qui a des expositions
            message = "Erreur : Impossible de supprimer la galerie '" + galerie.getNom() + "', il faut d'abord supprimer ses expositions";
        }
        // RedirectAttributes permet de transmettre des informations lors d'une redirection,
        // Ici on transmet un message de succès ou d'erreur
        // Ce message est accessible et affiché dans la vue 'afficheGalerie.html'
        redirectInfo.addFlashAttribute("message", message);
        return "redirect:show"; // on se redirige vers l'affichage de la liste
    }
    
    /**
     * Affiche tous les tableaux dans la base
     *
     * @param model pour transmettre les informations à la vue
     * @return le nom de la vue à afficher ('afficheTableaux.html')
     */
    @GetMapping(path = "showTableaux")
    public String afficheTousLesTableaux(Model model) {
        model.addAttribute("tableaux", tdao.findAll());
        return "afficheTableaux";
    }
    
    /**
     * Montre le formulaire permettant d'ajouter un tableau
     *
     * @param tableau initialisé par Spring, valeurs par défaut à afficher dans le formulaire
     * @param artistes liste des artistes
     * @return le nom de la vue à afficher ('formulaireTableau.html')
     */
    @GetMapping(path = "addTab")
    public String formulairePourAjoutTableau(@ModelAttribute("tableau") Tableau tableau, Model model){//, ArrayList<Artiste> artistes) {
        model.addAttribute("artistes", adao.findAll());
        return "formulaireTableau";
    }

    /**
     * Appelé par 'formulaireTableau.html', méthode POST
     *
     * @param tableau Un tableau initialisé avec les valeurs saisies dans le formulaire
     * @param redirectInfo pour transmettre des paramètres lors de la redirection
     * @return une redirection vers l'affichage de la liste des tableaux
     */
    @PostMapping(path = "saveTab")
    public String ajouteLaTableauPuisMontreLaListe(Tableau tableau, RedirectAttributes redirectInfo) {
        String message;
        try {
            //System.out.println("\n\n\n___________________ "+tableau.getAuteur().toString()+" ___________________\n\n\n");
            tdao.save(tableau);
            message = "Le tableau '" + tableau.getTitre() + "' a été correctement enregistré";
        } catch (DataIntegrityViolationException e) {
            message = "Erreur : impossible d'enregistrer le tableau '" + tableau.getTitre();
        }
        redirectInfo.addFlashAttribute("message", message);
        return "redirect:showTableaux";		
    }

    /**
     * Appelé par le lien 'Supprimer' dans 'afficheTableau.html'
     *
     * @param tableau à partir de l'id du tableau transmis en paramètre, Spring fera une requête SQL SELECT pour
     * chercher les tableaux dans la base
     * @param redirectInfo pour transmettre des paramètres lors de la redirection
     * @return une redirection vers l'affichage de la liste des tableaux
     */
    @GetMapping(path = "deleteTab")
    public String supprimeUneCategoriePuisMontreLaListe(@RequestParam("id") Tableau tableau, RedirectAttributes redirectInfo) {
        String message = "Le tableau '" + tableau.getTitre() + "' a bien été supprimée";
        try {
            tdao.delete(tableau);
        } catch (DataIntegrityViolationException e) {
            message = "Erreur : Impossible de supprimer le tableau '" + tableau.getTitre();
        }
        redirectInfo.addFlashAttribute("message", message);
        return "redirect:showTableaux";
    }
}
