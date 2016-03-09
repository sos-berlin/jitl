package com.sos.jitl.eventing;

/**
 *
 */
/** @author KB */

// TODO <start_job> started auch jobs, die gestopped oder suspended sind.
// Abhilfe: start-job ausprogrammieren
// TODO Events im PostProcessing löschen komt zu spät. Im actions.xml sofort ein
// Rename auf die Events und dann auch die renamed events löschen
// TODO Process class erzeugen mit "30"
// TODO Events ohne Vorgänger werden im postprocessing nicht gelöscht. sollten
// aber.
// TODO Für die externen Events jeweils einen Job schreiben, der die setzt.
// TODO state-text für shell-scripte über eine Datei im monitor holen und dann
// setzen
// TODO JS Objete als Job, JC und Order erzeugen.
// TODO Jobs generieren als SSH-Jobs
// TODO JOC -> knopf, um eine Task-Queue komplett zu löschen

// TODO eine "echte" Event-Queue implementieren. Im Moment sind die "Events" nur
// reine Statuse.
// TODO im actions.xml Variable erlauben (z.B. die LOADID). Dann läuft ein
// event-handler z.B. für eine bestimmte LoadID
// in der Order ein Tooken mitgeben was dann als ID für die Variable verwendet
// werden kann (ähnlich ODAT z.B.).
// woher bekommt das Tooken seinen Wert? evtl. doch mit Job,JC und Order
// arbeiten

