(function () {
  /**
   * Vérifications des dépendances
   */
  if (typeof jQuery === 'undefined') { console.error('jQuery absent'); return; }
  if (!$.fn || typeof $.fn.DataTable === 'undefined') { console.error('DataTables absent'); return; }
  if (typeof AppConfig === 'undefined') { console.error('AppConfig (contenant les URLs) est absent.'); return; }

  // ==========================================================================
  // 1. GESTION DE L'ÉTAT (STATE MANAGEMENT)
  // ==========================================================================

  const State = {
    currentId: null,      // ID de l'étudiant sélectionné
    currentData: null,    // Données complètes de l'étudiant sélectionné
    editMode: false,      // Si on est en mode édition

    // Caches pour les données référentielles (chargées une seule fois)
    refs: {
      entreprises: null, // [{id, nom}]
      statuts: null,     // [{id, nom}]
      profs: null        // [{id, nom, prenom, login}]
    },

    // Cache local pour les fiches étudiants complètes
    // Map: id -> objet étudiant complet
    studentsCache: new Map()
  };

  // ==========================================================================
  // 2. SERVICE API & CACHE (DATA LAYER)
  // ==========================================================================

  const Api = {
    /**
     * Charge la liste des entreprises (avec mise en cache)
     */
    loadEntreprises: function () {
      if (State.refs.entreprises) return Promise.resolve(State.refs.entreprises);
      return fetch(AppConfig.urlEnt, { headers: { 'Accept': 'application/json' } })
        .then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
        .then(list => (State.refs.entreprises = list || []));
    },

    /**
     * Charge la liste des statuts (avec mise en cache)
     */
    loadStatuts: function () {
      if (State.refs.statuts) return Promise.resolve(State.refs.statuts);
      return fetch(AppConfig.urlStatut, { headers: { 'Accept': 'application/json' } })
        .then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
        .then(list => (State.refs.statuts = list || []));
    },

    /**
     * Charge la liste des professeurs (avec mise en cache)
     */
    loadProfesseurs: function () {
      if (State.refs.profs) return Promise.resolve(State.refs.profs);
      return fetch(AppConfig.urlProf, { headers: { 'Accept': 'application/json' } })
        .then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
        .then(list => (State.refs.profs = list || []));
    },

    /**
     * Récupère la fiche complète d'un étudiant.
     * Utilise le cache local si disponible.
     */
    getEtudiantFull: function (id) {
      // Vérification du cache
      if (State.studentsCache.has(id)) {
        console.log(`[Cache] Chargement étudiant ${id} depuis le cache local.`);
        return Promise.resolve(State.studentsCache.get(id));
      }

      // Appel réseau si pas en cache
      const url = AppConfig.urlBase + '/' + id;
      return fetch(url, { headers: { 'Accept': 'application/json' } })
        .then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
        .then(data => {
          // Mise en cache
          State.studentsCache.set(id, data);
          return data;
        });
    },

    /**
     * Sauvegarde les modifications d'un étudiant (PUT)
     */
    saveEtudiant: function (id, payload) {
      return fetch(`${AppConfig.urlBase}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload)
      })
        .then(r => {
          if (r.status === 400) { return r.text().then(t => Promise.reject(new Error(t))); }
          if (!r.ok) throw new Error('HTTP ' + r.status);
          return r.json();
        })
        .then(data => {
          // Mise à jour du cache avec les nouvelles données
          State.studentsCache.set(id, data);
          return data;
        });
    }
  };

  // ==========================================================================
  // 3. UTILITAIRES UI (SPINNER, FORMATAGE)
  // ==========================================================================

  const UI = {
    loaderTimeout: null,

    /**
     * Affiche le spinner avec un délai (Debounce).
     * Le spinner ne s'affiche que si l'opération prend plus de 300ms.
     */
    showLoader: function () {
      if (this.loaderTimeout) clearTimeout(this.loaderTimeout);
      this.loaderTimeout = setTimeout(() => {
        $('#loading-overlay').show();
      }, 300); // 300ms de délai
    },

    /**
     * Masque le spinner et annule le délai s'il n'est pas encore affiché.
     */
    hideLoader: function () {
      if (this.loaderTimeout) {
        clearTimeout(this.loaderTimeout);
        this.loaderTimeout = null;
      }
      $('#loading-overlay').hide();
    },

    /**
     * Formate une date ISO pour l'affichage
     */
    formatDateTime: function (isoString) {
      if (!isoString) return '';
      return dayjs(isoString).format('YYYY-MM-DD HH:mm:ss');
    },
    /**
 * Formate une date ISO pour l'affichage SANS l'heure
 * (équivalent d'un <fmt:formatDate pattern="dd/MM/yyyy" /> côté JSP)
 */
    formatDate: function (isoString) {
      if (!isoString) return '';
      return dayjs(isoString).format('DD/MM/YYYY');
    },


    /**
     * Convertit date + heure locales vers ISO pour l'envoi
     */
    toIsoLocal: function (dateStr, timeStr) {
      if (!dateStr) return null;
      return dayjs(`${dateStr} ${timeStr || '00:00'}`).format('YYYY-MM-DD HH:mm:ss');
    }
  };

  // ==========================================================================
  // 4. LOGIQUE DE RENDU (VIEWS)
  // ==========================================================================

  /**
   * Affiche la fiche en mode LECTURE SEULE
   */
  function renderFicheView(e) {
    const template = document.getElementById('template-fiche-lecture').content.cloneNode(true);
    const $fiche = $(template);

    // Remplissage des champs simples
    $fiche.find('[data-field="nom"]').text(e.nom ?? '-');
    $fiche.find('[data-field="prenom"]').text(e.prenom ?? '-');
    $fiche.find('[data-field="titre"]').text(e.titre ?? '-');
    $fiche.find('[data-field="typeStage"]').text(e.typeStage ?? '-');
    $fiche.find('[data-field="signee"]').text(e.signee === true ? 'Oui' : e.signee === false ? 'Non' : '-');
    $fiche.find('[data-field="annee"]').text(e.annee ?? '-');
    $fiche.find('[data-field="entreprise"]').text(e.entreprise ?? '-');
    // Affichage "soutenance / stage" sans heure
    $fiche.find('[data-field="dateDebut"]').text(UI.formatDate(e.dateDebut) || '-');
    $fiche.find('[data-field="dateFin"]').text(UI.formatDate(e.dateFin) || '-');

    $fiche.find('[data-field="statut"]').text(e.statut ?? '-');
    $fiche.find('[data-field="confidentiel"]').text(e.confidentiel === true ? 'Oui' : e.confidentiel === false ? 'Non' : '-');
    $fiche.find('[data-field="maitreStage"]').text(e.maitreStage === true ? 'Oui' : e.maitreStage === false ? 'Non' : '-');
    $fiche.find('[data-field="reponse"]').text(e.reponse === true ? 'Oui' : e.reponse === false ? 'Non' : '-');
    $fiche.find('[data-field="note"]').text(e.note ?? '-');

    // Remplissage des listes (Présentations & Jury)
    const presHtml = (e.presentations ?? []).map(p =>
      `<li>
        <div><strong>Lieu:</strong> ${p.lieu ?? '-'} (${p.typeLieu ?? 'N/A'})</div>
        <div><strong>Date:</strong> ${UI.formatDateTime(p.datePresentee) || '-'}</div>
      </li>`
    ).join('');
    if (presHtml) $fiche.find('[data-list="presentations"]').html(presHtml);

    const juryHtml = (e.jury ?? []).map(j =>
      `<li>${j.role ?? '-'} — ${j.nom ?? '-'} ${j.prenom ?? ''} (${j.login ?? '-'})</li>`
    ).join('');
    if (juryHtml) $fiche.find('[data-list="jury"]').html(juryHtml);

    // Injection dans le DOM
    $('#ficheContent').html($fiche);

    // Gestion de la visibilité des boutons
    $('#ficheActions').show();
    $('#btnEdit').show();
    $('#btnSave, #btnCancel').hide();
    State.editMode = false;
  }

  /**
   * Affiche la fiche en mode ÉDITION
   */
  function renderFicheEdit(e) {
    UI.showLoader();

    // On s'assure d'avoir tous les référentiels avant d'afficher le formulaire
    Promise.all([Api.loadEntreprises(), Api.loadStatuts(), Api.loadProfesseurs()])
      .then(([ents, stats, profs]) => {
        const template = document.getElementById('template-fiche-edition').content.cloneNode(true);
        const $fiche = $(template);

        // --- Remplissage des inputs ---
        $fiche.find('[data-field="nom"]').val(e.nom ?? '');
        $fiche.find('[data-field="prenom"]').val(e.prenom ?? '');
        $fiche.find('[data-field="titre"]').val(e.titre ?? '');
        $fiche.find('[data-field="dateDebut"]').val(UI.formatDateTime(e.dateDebut) || '');
        $fiche.find('[data-field="dateFin"]').val(UI.formatDateTime(e.dateFin) || '');
        $fiche.find('[data-field="typeStage"]').val(e.typeStage ?? '');
        $fiche.find('[data-field="signee"]').prop('checked', e.signee === true);
        $fiche.find('[data-field="annee"]').val(e.annee ?? '');
        $fiche.find('[data-field="confidentiel"]').prop('checked', e.confidentiel === true);
        $fiche.find('[data-field="maitreStage"]').prop('checked', e.maitreStage === true);
        $fiche.find('[data-field="reponse"]').prop('checked', e.reponse === true);
        $fiche.find('[data-field="note"]').val(e.note ?? '');

        // --- Remplissage des Selects (Référentiels) ---
        const optsEnt = ['<option value="">-- Choisir --</option>'].concat(ents.map(x => `<option value="${x.id}">${x.nom}</option>`)).join('');
        $fiche.find('[data-field="entrepriseId"]').html(optsEnt).val(e.entrepriseId ?? '');

        const optsSta = ['<option value="">-- Choisir --</option>'].concat(stats.map(x => `<option value="${x.id}">${x.nom}</option>`)).join('');
        $fiche.find('[data-field="statutId"]').html(optsSta).val(e.statutId ?? '');

        const optsProf = ['<option value="">-- Choisir --</option>'].concat(profs.map(p => `<option value="${p.id}">${p.nom} ${p.prenom} (${p.login})</option>`)).join('');
        $fiche.find('[data-field="presidentId"]').html(optsProf);
        $fiche.find('[data-field="rapporteurId"]').html(optsProf);

        // --- Pré-sélection du Jury ---
        const juryList = e.jury ?? [];
        const pj = juryList.find(j => j.role === 'Président');
        const rj = juryList.find(j => j.role === 'Rapporteur');
        if (pj && pj.professeurId) $fiche.find('[data-field="presidentId"]').val(pj.professeurId);
        if (rj && rj.professeurId) $fiche.find('[data-field="rapporteurId"]').val(rj.professeurId);

        // --- Remplissage de la table des présentations ---
        const presRows = (e.presentations ?? []).map(p => {
          const d = p.datePresentee ? String(p.datePresentee).split('T')[0] : ''; // yyyy-MM-dd
          const t = p.heure ? (String(p.heure).split('T')[1] || '').slice(0, 5) : ''; // HH:mm
          return `<tr>
                  <td><input type="date" class="inpPresDate" value="${d}"></td>
                  <td><input type="time" class="inpPresHeure" value="${t}"></td>
                  <td><input type="text" class="inpPresLieu" value="${p.lieu ?? ''}" placeholder="ex: Amphi A / Zoom"></td>
                  <td>
                    <select class="selPresType">
                      <option value="">-</option>
                      <option value="presentiel" ${p.typeLieu === 'presentiel' ? 'selected' : ''}>presentiel</option>
                      <option value="distanciel" ${p.typeLieu === 'distanciel' ? 'selected' : ''}>distanciel</option>
                      <option value="autre" ${p.typeLieu === 'autre' ? 'selected' : ''}>autre</option>
                    </select>
                  </td>
                  <td><button type="button" class="btnDelPres">Supprimer</button></td>
                </tr>`;
        }).join('');
        $fiche.find('#tblPres tbody').html(presRows);

        // Injection
        $('#ficheContent').html($fiche);

        // --- Attachement des événements spécifiques au mode édition ---

        // Ajout d'une ligne de présentation
        $('#btnAddPres').on('click', function () {
          $('#tblPres tbody').append(`
          <tr>
            <td><input type="date" class="inpPresDate"></td>
            <td><input type="time" class="inpPresHeure"></td>
            <td><input type="text" class="inpPresLieu" placeholder="ex: Amphi A / Zoom"></td>
            <td>
              <select class="selPresType">
                <option value="">-</option>
                <option value="presentiel">presentiel</option>
                <option value="distanciel">distanciel</option>
                <option value="autre">autre</option>
              </select>
            </td>
            <td><button type="button" class="btnDelPres">Supprimer</button></td>
          </tr>
        `);
        });

        // Suppression d'une ligne de présentation (délégation d'événement)
        $('#tblPres').on('click', '.btnDelPres', function () {
          $(this).closest('tr').remove();
        });

        // Gestion des boutons globaux
        $('#btnEdit').hide();
        $('#btnSave, #btnCancel').show();
        State.editMode = true;

      }).catch(err => {
        console.error('Référentiels:', err);
        $('#ficheContent').html('<span class="muted">Impossible de charger les référentiels.</span>');
      })
      .finally(() => {
        UI.hideLoader();
      });
  }

  // ==========================================================================
  // 5. ORCHESTRATION & ÉVÉNEMENTS
  // ==========================================================================

  /**
   * Charge et affiche la fiche d'un étudiant
   */
  function chargerFiche(id) {
    State.currentId = id;
    $('#ficheContent').html('<span class="muted">Chargement…</span>');

    UI.showLoader();
    Api.getEtudiantFull(id)
      .then(e => {
        State.currentData = e;
        renderFicheView(e);
      })
      .catch(err => {
        console.error('Erreur fiche full:', err);
        $('#ficheContent').html('<span class="muted">Impossible de charger la fiche.</span>');
      })
      .finally(() => {
        UI.hideLoader();
      });
  }

  /**
   * Sauvegarde les données du formulaire
   */
  function handleSave() {
    if (!State.currentId) return;

    // Validation basique
    const vPres = $('[data-field="presidentId"]').val();
    const vRapp = $('[data-field="rapporteurId"]').val();
    if (vPres && vRapp && vPres === vRapp) {
      alert('Le président et le rapporteur doivent être deux enseignants différents.');
      return;
    }
    const presidentId = vPres ? parseInt(vPres, 10) : null;
    const rapporteurId = vRapp ? parseInt(vRapp, 10) : null;

    // Construction de la liste des présentations
    const presList = [];
    $('#tblPres tbody tr').each(function () {
      const d = $(this).find('.inpPresDate').val();
      const h = $(this).find('.inpPresHeure').val();
      const lieu = $(this).find('.inpPresLieu').val()?.trim() || null;
      const type = $(this).find('.selPresType').val() || null;

      if (!d && !h && !lieu) return; // ignore ligne vide

      presList.push({
        datePresentee: UI.toIsoLocal(d, h),
        heure: h ? UI.toIsoLocal('0001-01-01', h) : null,
        lieu: lieu,
        typeLieu: type
      });
    });

    // Construction du payload complet
    const payload = {
      // Étudiant
      nom: $('[data-field="nom"]').val()?.trim() || '',
      prenom: $('[data-field="prenom"]').val()?.trim() || '',
      // Stage
      titre: $('[data-field="titre"]').val()?.trim() || '',
      dateDebut: $('[data-field="dateDebut"]').val()?.trim() || null,
      dateFin: $('[data-field="dateFin"]').val()?.trim() || null,
      typeStage: $('[data-field="typeStage"]').val()?.trim() || null,
      signee: $('[data-field="signee"]').is(':checked'),
      annee: $('[data-field="annee"]').val()?.trim() || null,
      entrepriseId: (function () { const v = $('[data-field="entrepriseId"]').val(); return v ? parseInt(v, 10) : null; })(),
      // Soutenance
      confidentiel: $('[data-field="confidentiel"]').is(':checked'),
      maitreStage: $('[data-field="maitreStage"]').is(':checked'),
      reponse: $('[data-field="reponse"]').is(':checked'),
      note: $('[data-field="note"]').val()?.trim() || null,
      statutId: (function () { const v = $('[data-field="statutId"]').val(); return v ? parseInt(v, 10) : null; })(),
      // Jury
      presidentId: presidentId,
      rapporteurId: rapporteurId,
      // Présentations
      presentations: presList
    };

    // Envoi
    $('#ficheContent').html('<span class="muted">Enregistrement…</span>');
    UI.showLoader();

    Api.saveEtudiant(State.currentId, payload)
      .then(e => {
        State.currentData = e;
        renderFicheView(e);
        // Rafraîchir la liste sans changer la page
        table.ajax.reload(null, false);
      })
      .catch(err => {
        console.error('Erreur save full:', err);
        $('#ficheContent').html('<span class="muted">Échec: ' + err.message + '</span>');
      })
      .finally(() => {
        UI.hideLoader();
      });
  }

  // ==========================================================================
  // 6. INITIALISATION
  // ==========================================================================

  // Configuration de la DataTable
  const table = $('#tEtudiants').DataTable({
    ajax: {
      url: AppConfig.urlList,
      dataSrc: '',
      error: function (xhr) { console.error('Ajax DataTables:', xhr.status, xhr.responseText); }
    },
    columns: [
      { data: 'nom' },
      { data: 'prenom' },
      { data: 'typeStage' },
      { data: 'entreprise' }
    ],
    pageLength: 10,
    createdRow: function (row, data) {
      $(row).addClass('row-hover');

      // Récupération du statut de soutenance (texte)
      const statutRaw = (data.statut || '').toString().toLowerCase().trim();

      let cssClass = null;

      if (statutRaw === 'passée' || statutRaw === 'passee') {
        cssClass = 'row-passee';       // vert
      } else if (statutRaw === 'annulée' || statutRaw === 'annulee') {
        cssClass = 'row-annulee';      // rouge
      } else if (statutRaw === 'reportée' || statutRaw === 'reportee') {
        cssClass = 'row-reportee';     // orange
      } else if (statutRaw === 'à venir' || statutRaw === 'a venir' || statutRaw === 'avenir') {
        cssClass = 'row-avenir';       // jaune
      } else if (!statutRaw || statutRaw === 'non défini' || statutRaw === 'non defini') {
        // Aucun statut réellement choisi
        cssClass = 'row-undefined';    // rouge foncé
      }

      if (cssClass) {
        $(row).addClass(cssClass);
      }
    }

  });

  // Événements DataTable
  table.on('xhr', function () {
    const data = table.ajax.json();
    if (!Array.isArray(data) || data.length === 0) {
      if (State.currentId == null) {
        $('#ficheContent').html('<span class="muted">Aucune donnée à afficher.</span>');
      }
      return;
    }
    // Sélection automatique du premier élément au chargement
    if (State.currentId == null) {
      $('#tEtudiants tbody tr').removeClass('selected');
      $('#tEtudiants tbody tr:eq(0)').addClass('selected');
      chargerFiche(data[0].id);
    }
  });

  $('#tEtudiants tbody').on('click', 'tr', function () {
    $('#tEtudiants tbody tr').removeClass('selected');
    $(this).addClass('selected');
    const rowData = table.row(this).data();
    if (rowData && rowData.id != null) chargerFiche(rowData.id);
  });

  // Boutons d'action globaux
  $('#btnEdit').on('click', function () { if (State.currentData) renderFicheEdit(State.currentData); });
  $('#btnCancel').on('click', function () { if (State.currentData) renderFicheView(State.currentData); });
  $('#btnSave').on('click', handleSave);

  // Chargement initial des référentiels en arrière-plan
  $(document).ready(function () {
    Api.loadEntreprises();
    Api.loadStatuts();
    Api.loadProfesseurs();
  });

})();