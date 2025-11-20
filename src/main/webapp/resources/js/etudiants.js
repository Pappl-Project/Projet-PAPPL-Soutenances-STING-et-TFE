(function () {
  if (typeof jQuery === 'undefined') {
    console.error('jQuery absent'); return;
  }
  if (!$.fn || typeof $.fn.DataTable === 'undefined') {
    console.error('DataTables absent'); return;
  }
  if (typeof AppConfig === 'undefined') {
    console.error('AppConfig (contenant les URLs) est absent.'); return;
  }

  // État local
  let currentId   = null;
  let currentData = null; // dernier JSON full reçu
  let editMode    = false;

  // Caches référentiels
  let entreprisesCache = null; // [{id, nom}]
  let statutsCache     = null; // [{id, nom}]
  let profsCache       = null; // [{id, nom, prenom, login}]

  // --- Fonctions pour le spinner de chargement ---
  function showLoader() {
    $('#loading-overlay').show();
  }

  function hideLoader() {
    $('#loading-overlay').hide();
  }

  function loadEntreprises() {
    if (entreprisesCache) return Promise.resolve(entreprisesCache);
    return fetch(AppConfig.urlEnt, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(list => (entreprisesCache = list || []));
  }
  function loadStatuts() {
    if (statutsCache) return Promise.resolve(statutsCache);
    return fetch(AppConfig.urlStatut, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(list => (statutsCache = list || []));
  }
  function loadProfesseurs() {
    if (profsCache) return Promise.resolve(profsCache);
    return fetch(AppConfig.urlProf, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(list => (profsCache = list || []));
  }

  // DataTables
  const table = $('#tEtudiants').DataTable({
    ajax: {
      url: AppConfig.urlList,
      dataSrc: '',
      error: function(xhr){ console.error('Ajax DataTables:', xhr.status, xhr.responseText); }
    },
    columns: [
      { data: 'nom' },
      { data: 'prenom' },
      { data: 'typeStage' },
      { data: 'entreprise' }
    ],
    pageLength: 10,
    createdRow: function(row){ $(row).addClass('row-hover'); }
  });

  // Chargement initial: fiche du premier
  table.on('xhr', function(){
    const data = table.ajax.json();
    if (!Array.isArray(data) || data.length === 0) {
      if (currentId == null) {
        $('#ficheContent').html('<span class="muted">Aucune donnée à afficher.</span>');
      }
      return;
    }
    if (currentId == null) {
      $('#tEtudiants tbody tr').removeClass('selected');
      $('#tEtudiants tbody tr:eq(0)').addClass('selected');
      chargerFiche(data[0].id);
    }
  });

  // Clic ligne -> fiche
  $('#tEtudiants tbody').on('click', 'tr', function(){
    $('#tEtudiants tbody tr').removeClass('selected');
    $(this).addClass('selected');
    const rowData = table.row(this).data();
    if (rowData && rowData.id != null) chargerFiche(rowData.id);
  });

  // --- Fonctions utilitaires modernisées (avec Day.js) ---

  // Formatage d’affichage des dates ISO -> "YYYY-MM-DD HH:mm:ss"
  function formatDateTime(isoString) {
    if (!isoString) return '';
    // dayjs() parse les formats ISO nativement
    return dayjs(isoString).format('YYYY-MM-DD HH:mm:ss');
  }

  // Conversion des inputs date/time locaux vers un format ISO partiel pour le backend
  function toIsoLocal(dateStr, timeStr) {
    if (!dateStr) return null;
    // On combine la date et l'heure, puis on formate. Day.js gère les cas où timeStr est vide.
    return dayjs(`${dateStr} ${timeStr || '00:00'}`).format('YYYY-MM-DD HH:mm:ss');
  }

  // Pré-chargement des données de référence en arrière-plan pour une meilleure réactivité
  $(document).ready(function() {
      loadEntreprises();
      loadStatuts();
      loadProfesseurs();
  });

  // ----- Vue lecture -----
  function renderFicheView(e){
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
    $fiche.find('[data-field="dateDebut"]').text(formatDateTime(e.dateDebut) || '-');
    $fiche.find('[data-field="dateFin"]').text(formatDateTime(e.dateFin) || '-');
    $fiche.find('[data-field="statut"]').text(e.statut ?? '-');
    $fiche.find('[data-field="confidentiel"]').text(e.confidentiel === true ? 'Oui' : e.confidentiel === false ? 'Non' : '-');
    $fiche.find('[data-field="maitreStage"]').text(e.maitreStage === true ? 'Oui' : e.maitreStage === false ? 'Non' : '-');
    $fiche.find('[data-field="reponse"]').text(e.reponse === true ? 'Oui' : e.reponse === false ? 'Non' : '-');
    $fiche.find('[data-field="note"]').text(e.note ?? '-');

    // Remplissage des listes
    const presHtml = (e.presentations ?? []).map(p => `<li><div><strong>Lieu:</strong> ${p.lieu ?? '-'} (${p.typeLieu ?? 'N/A'})</div><div><strong>Date:</strong> ${formatDateTime(p.datePresentee) || '-'}</div></li>`).join('');
    if (presHtml) $fiche.find('[data-list="presentations"]').html(presHtml);

    const juryHtml = (e.jury ?? []).map(j => `<li>${j.role ?? '-'} — ${j.nom ?? '-'} ${j.prenom ?? ''} (${j.login ?? '-'})</li>`).join('');
    if (juryHtml) $fiche.find('[data-list="jury"]').html(juryHtml);

    $('#ficheContent').html($fiche);

    $('#ficheActions').show();
    $('#btnEdit').show();
    $('#btnSave, #btnCancel').hide();
    editMode = false;
  }

  // ----- Vue édition -----
  function renderFicheEdit(e){
    showLoader();
    Promise.all([loadEntreprises(), loadStatuts(), loadProfesseurs()])
    .then(([ents, stats, profs]) => {
      const template = document.getElementById('template-fiche-edition').content.cloneNode(true);
      const $fiche = $(template);

      // Remplissage des champs simples
      $fiche.find('[data-field="nom"]').val(e.nom ?? '');
      $fiche.find('[data-field="prenom"]').val(e.prenom ?? '');
      $fiche.find('[data-field="titre"]').val(e.titre ?? '');
      $fiche.find('[data-field="dateDebut"]').val(formatDateTime(e.dateDebut) || '');
      $fiche.find('[data-field="dateFin"]').val(formatDateTime(e.dateFin) || '');
      $fiche.find('[data-field="typeStage"]').val(e.typeStage ?? '');
      $fiche.find('[data-field="signee"]').prop('checked', e.signee === true);
      $fiche.find('[data-field="annee"]').val(e.annee ?? '');
      $fiche.find('[data-field="confidentiel"]').prop('checked', e.confidentiel === true);
      $fiche.find('[data-field="maitreStage"]').prop('checked', e.maitreStage === true);
      $fiche.find('[data-field="reponse"]').prop('checked', e.reponse === true);
      $fiche.find('[data-field="note"]').val(e.note ?? '');

      // Remplissage des <select>
      const optsEnt = ['<option value="">-- Choisir --</option>'].concat(ents.map(x => `<option value="${x.id}">${x.nom}</option>`)).join('');
      $fiche.find('[data-field="entrepriseId"]').html(optsEnt).val(e.entrepriseId ?? '');

      const optsSta = ['<option value="">-- Choisir --</option>'].concat(stats.map(x => `<option value="${x.id}">${x.nom}</option>`)).join('');
      $fiche.find('[data-field="statutId"]').html(optsSta).val(e.statutId ?? '');

      const optsProf = ['<option value="">-- Choisir --</option>'].concat(profs.map(p => `<option value="${p.id}">${p.nom} ${p.prenom} (${p.login})</option>`)).join('');
      $fiche.find('[data-field="presidentId"]').html(optsProf);
      $fiche.find('[data-field="rapporteurId"]').html(optsProf);

      // Pré-sélections
      const juryList = e.jury ?? [];
      const pj = juryList.find(j => j.role === 'Président');
      const rj = juryList.find(j => j.role === 'Rapporteur');
      if (pj && pj.professeurId) $fiche.find('[data-field="presidentId"]').val(pj.professeurId);
      if (rj && rj.professeurId) $fiche.find('[data-field="rapporteurId"]').val(rj.professeurId);

      // Remplissage de la table des présentations
      const presRows = (e.presentations ?? []).map(p => {
        const d = p.datePresentee ? String(p.datePresentee).split('T')[0] : ''; // yyyy-MM-dd
        const t = p.heure ? (String(p.heure).split('T')[1] || '').slice(0,5) : ''; // HH:mm
        return `<tr>
                  <td><input type="date" class="inpPresDate" value="${d}"></td>
                  <td><input type="time" class="inpPresHeure" value="${t}"></td>
                  <td><input type="text" class="inpPresLieu" value="${p.lieu ?? ''}" placeholder="ex: Amphi A / Zoom"></td>
                  <td>
                    <select class="selPresType">
                      <option value="">-</option>
                      <option value="presentiel" ${p.typeLieu==='presentiel'?'selected':''}>presentiel</option>
                      <option value="distanciel" ${p.typeLieu==='distanciel'?'selected':''}>distanciel</option>
                      <option value="autre" ${p.typeLieu==='autre'?'selected':''}>autre</option>
                    </select>
                  </td>
                  <td><button type="button" class="btnDelPres">Supprimer</button></td>
                </tr>`;
      }).join('');
      $fiche.find('#tblPres tbody').html(presRows);

      $('#ficheContent').html($fiche);

      // Handlers Présentations
      $('#btnAddPres').on('click', function(){
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
      $('#tblPres').on('click', '.btnDelPres', function(){
        $(this).closest('tr').remove();
      });

      $('#btnEdit').hide();
      $('#btnSave, #btnCancel').show();
      editMode = true;

    }).catch(err => {
      console.error('Référentiels:', err);
      $('#ficheContent').html('<span class="muted">Impossible de charger les référentiels.</span>');
    })
    .finally(() => {
      hideLoader();
    });
  }

  function chargerFiche(id){
    currentId = id; 
    $('#ficheContent').html('<span class="muted">Chargement…</span>');
    const url = AppConfig.urlBase + '/' + id;
    
    showLoader();
    fetch(url, { headers: { 'Accept':'application/json' }})
      .then(r => { if (!r.ok) throw new Error('HTTP '+r.status); return r.json(); })
      .then(e => { currentData = e; renderFicheView(e); })
      .catch(err => {
        console.error('Erreur fiche full:', err);
        $('#ficheContent').html('<span class="muted">Impossible de charger la fiche.</span>');
      })
      .finally(() => {
        hideLoader();
      });
  }

  // Boutons
  $('#btnEdit').on('click', function(){ if (currentData) renderFicheEdit(currentData); });
  $('#btnCancel').on('click', function(){ if (currentData) renderFicheView(currentData); });
  $('#btnSave').on('click', function(){
    if (!currentId) return;

    // Jury
    const vPres = $('[data-field="presidentId"]').val();
    const vRapp = $('[data-field="rapporteurId"]').val();
    if (vPres && vRapp && vPres === vRapp) {
      alert('Le président et le rapporteur doivent être deux enseignants différents.');
      return;
    }
    const presidentId  = vPres ? parseInt(vPres, 10) : null;
    const rapporteurId = vRapp ? parseInt(vRapp, 10) : null;

    // Présentations -> payload
    const presList = [];
    $('#tblPres tbody tr').each(function(){
      const d    = $(this).find('.inpPresDate').val();
      const h    = $(this).find('.inpPresHeure').val();
      const lieu = $(this).find('.inpPresLieu').val()?.trim() || null;
      const type = $(this).find('.selPresType').val() || null;
      if (!d && !h && !lieu) return; // ignore ligne totalement vide
      presList.push({
        datePresentee: toIsoLocal(d, h),                  // "yyyy-MM-dd HH:mm:ss" ou null
        heure: h ? toIsoLocal('0001-01-01', h) : null,   // si tu gardes un champ heure séparé
        lieu: lieu,
        typeLieu: type
      });
    });

    const payload = {
      // Étudiant
      nom:        $('[data-field="nom"]').val()?.trim() || '',
      prenom:     $('[data-field="prenom"]').val()?.trim() || '',
      // Stage
      titre:      $('[data-field="titre"]').val()?.trim() || '',
      dateDebut:  $('[data-field="dateDebut"]').val()?.trim() || null,
      dateFin:    $('[data-field="dateFin"]').val()?.trim() || null,
      typeStage:  $('[data-field="typeStage"]').val()?.trim() || null,
      signee:     $('[data-field="signee"]').is(':checked'),
      annee:      $('[data-field="annee"]').val()?.trim() || null,
      entrepriseId: (function(){ const v = $('[data-field="entrepriseId"]').val(); return v ? parseInt(v,10) : null; })(),
      // Soutenance
      confidentiel: $('[data-field="confidentiel"]').is(':checked'),
      maitreStage:  $('[data-field="maitreStage"]').is(':checked'),
      reponse:      $('[data-field="reponse"]').is(':checked'),
      note:         $('[data-field="note"]').val()?.trim() || null,
      statutId: (function(){ const v = $('[data-field="statutId"]').val(); return v ? parseInt(v,10) : null; })(),
      // Jury
      presidentId:  presidentId,
      rapporteurId: rapporteurId,
      // Présentations
      presentations: presList
    };

    showLoader();
    fetch(`${AppConfig.urlBase}/${currentId}`, {
      method: 'PUT',
      headers: { 'Content-Type':'application/json', 'Accept':'application/json' },
      body: JSON.stringify(payload)
    })
    .then(r => {
      if (r.status === 400) { return r.text().then(t => Promise.reject(new Error(t))); }
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(e => {
      currentData = e;
      renderFicheView(e);
      table.ajax.reload(null, false); // garde la même page/tri
    })
    .catch(err => {
      console.error('Erreur save full:', err);
      $('#ficheContent').html('<span class="muted">Échec: ' + err.message + '</span>');
    })
    .finally(() => {
      hideLoader();
    });
  });
})();